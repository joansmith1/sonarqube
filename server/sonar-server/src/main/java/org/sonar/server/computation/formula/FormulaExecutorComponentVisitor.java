/*
 * SonarQube, open source software quality management tool.
 * Copyright (C) 2008-2014 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * SonarQube is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * SonarQube is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.server.computation.formula;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.CheckForNull;
import org.sonar.server.computation.component.Component;
import org.sonar.server.computation.component.PathAwareVisitor;
import org.sonar.server.computation.measure.Measure;
import org.sonar.server.computation.measure.MeasureRepository;
import org.sonar.server.computation.metric.Metric;
import org.sonar.server.computation.metric.MetricRepository;
import org.sonar.server.computation.period.Period;
import org.sonar.server.computation.period.PeriodsHolder;

import static java.util.Objects.requireNonNull;

public class FormulaExecutorComponentVisitor extends PathAwareVisitor<FormulaExecutorComponentVisitor.Counters> {
  private static final PathAwareVisitor.SimpleStackElementFactory<Counters> COUNTERS_FACTORY = new PathAwareVisitor.SimpleStackElementFactory<Counters>() {

    @Override
    public Counters createForAny(Component component) {
      return new Counters();
    }

    @Override
    public Counters createForFile(Component component) {
      // No need to create a counter on file levels
      return null;
    }
  };

  @CheckForNull
  private final PeriodsHolder periodsHolder;
  private final MetricRepository metricRepository;
  private final MeasureRepository measureRepository;
  private final List<Formula> formulas;

  private FormulaExecutorComponentVisitor(Builder builder, List<Formula> formulas) {
    super(Component.Type.FILE, Order.POST_ORDER, COUNTERS_FACTORY);
    this.periodsHolder = builder.periodsHolder;
    this.measureRepository = builder.measureRepository;
    this.metricRepository = builder.metricRepository;
    this.formulas = ImmutableList.copyOf(formulas);
  }

  public static Builder newBuilder(MetricRepository metricRepository, MeasureRepository measureRepository) {
    return new Builder(metricRepository, measureRepository);
  }

  public static class Builder {
    private final MetricRepository metricRepository;
    private final MeasureRepository measureRepository;
    @CheckForNull
    private PeriodsHolder periodsHolder;

    private Builder(MetricRepository metricRepository, MeasureRepository measureRepository) {
      this.metricRepository = requireNonNull(metricRepository);
      this.measureRepository = requireNonNull(measureRepository);
    }

    public Builder create(MetricRepository metricRepository, MeasureRepository measureRepository) {
      return new Builder(metricRepository, measureRepository);
    }

    public Builder withVariationSupport(PeriodsHolder periodsHolder) {
      this.periodsHolder = requireNonNull(periodsHolder);
      return this;
    }

    public FormulaExecutorComponentVisitor buildFor(List<Formula> formulas) {
      return new FormulaExecutorComponentVisitor(this, formulas);
    }
  }

  @Override
  protected void visitProject(Component project, Path<FormulaExecutorComponentVisitor.Counters> path) {
    processNotFile(project, path);
  }

  @Override
  protected void visitModule(Component module, Path<FormulaExecutorComponentVisitor.Counters> path) {
    processNotFile(module, path);
  }

  @Override
  protected void visitDirectory(Component directory, Path<FormulaExecutorComponentVisitor.Counters> path) {
    processNotFile(directory, path);
  }

  @Override
  protected void visitFile(Component file, Path<FormulaExecutorComponentVisitor.Counters> path) {
    processFile(file, path);
  }

  private void processNotFile(Component component, Path<FormulaExecutorComponentVisitor.Counters> path) {
    for (Formula formula : formulas) {
      Counter counter = path.current().getCounter(formula);
      // If there were no file under this node, the counter won't be initialized
      if (counter != null) {
        for (String metricKey : formula.getOutputMetricKeys()) {
          addNewMeasure(component, metricKey, formula, counter);
        }
        aggregateToParent(path, formula, counter);
      }
    }
  }

  private void processFile(Component file, Path<FormulaExecutorComponentVisitor.Counters> path) {
    FileAggregateContext counterContext = new FileAggregateContextImpl(file);
    for (Formula formula : formulas) {
      Counter counter = formula.createNewCounter();
      counter.aggregate(counterContext);
      for (String metricKey : formula.getOutputMetricKeys()) {
        addNewMeasure(file, metricKey, formula, counter);
      }
      aggregateToParent(path, formula, counter);
    }
  }

  private void addNewMeasure(Component component, String metricKey, Formula formula, Counter counter) {
    Metric metric = metricRepository.getByKey(metricKey);
    Optional<Measure> measure = formula.createMeasure(counter, new CreateMeasureContextImpl(component, metric));
    if (measure.isPresent()) {
      measureRepository.add(component, metric, measure.get());
    }
  }

  private void aggregateToParent(Path<FormulaExecutorComponentVisitor.Counters> path, Formula formula, Counter currentCounter) {
    if (!path.isRoot()) {
      path.parent().aggregate(formula, currentCounter);
    }
  }

  private class FileAggregateContextImpl implements FileAggregateContext {
    private final Component file;

    public FileAggregateContextImpl(Component file) {
      this.file = file;
    }

    @Override
    public Component getFile() {
      return file;
    }

    @Override
    public Optional<Measure> getMeasure(String metricKey) {
      return measureRepository.getRawMeasure(file, metricRepository.getByKey(metricKey));
    }

    @Override
    public List<Period> getPeriods() {
      return periodsHolder.getPeriods();
    }
  }

  public static class Counters {
    Map<Formula, Counter> countersByFormula = new HashMap<>();

    public void aggregate(Formula formula, Counter childCounter) {
      Counter counter = countersByFormula.get(formula);
      if (counter == null) {
        countersByFormula.put(formula, childCounter);
      } else {
        counter.aggregate(childCounter);
      }
    }

    /**
     * Counter can be null on a level when it has not been fed by children levels
     */
    @CheckForNull
    public Counter getCounter(Formula formula) {
      return countersByFormula.get(formula);
    }
  }

  private class CreateMeasureContextImpl implements CreateMeasureContext {
    private final Component component;
    private final Metric metric;

    public CreateMeasureContextImpl(Component component, Metric metric) {
      this.component = component;
      this.metric = metric;
    }

    @Override
    public Component getComponent() {
      return component;
    }

    @Override
    public Metric getMetric() {
      return metric;
    }

    @Override
    public List<Period> getPeriods() {
      return periodsHolder.getPeriods();
    }
  }
}
