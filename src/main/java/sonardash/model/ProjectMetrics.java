package sonardash.model;

import lombok.Getter;
import lombok.Setter;
import sonardash.model.Resource.Metric;

import java.text.DecimalFormat;
import java.util.Map;

@Getter
public class ProjectMetrics {

    private static final String INTEGER_PATTERN = "#,##0";

    private static final String DOUBLE_PATTERN = "#,##0.0";

    private final String key;

    private final String name;

    private final ProjectMetric lines = new ProjectMetric(INTEGER_PATTERN);

    private final ProjectMetric classes = new ProjectMetric(INTEGER_PATTERN);

    private final ProjectMetric classComplexity = new ProjectMetric(DOUBLE_PATTERN);

    private final ProjectMetric violations = new ProjectMetric(INTEGER_PATTERN);

    private final ProjectMetric coverage = new ProjectMetric("#,##0.0'%'");

    private final ProjectMetric testExecutionTime = new ProjectMetric("#,##0.0s", "(+#,##0ms);(-#,##0ms)");

    private final ProjectMetric skippedTests = new ProjectMetric(INTEGER_PATTERN);

    public ProjectMetrics(Project project) {
        this(project.getKey(), project.getName());
    }

    public ProjectMetrics(String key, String name) {
        this.key = key;
        this.name = name;
    }

    public String getName(String filter) {
        return (filter != null) ? name.replaceAll(filter, "") : name;
    }

    private Double getValue(Resource resource, MetricDefinition metricDefinition) {
        return resource.getMetric(metricDefinition).map(Metric::getValue).orElse(0.0);
    }

    public void setValues(Resource resource) {
        lines.value = getValue(resource, MetricDefinition.ncloc);
        classes.value = getValue(resource, MetricDefinition.classes);
        classComplexity.value = getValue(resource, MetricDefinition.class_complexity);
        violations.value = getValue(resource, MetricDefinition.blocker_violations)
                + getValue(resource, MetricDefinition.critical_violations)
                + getValue(resource, MetricDefinition.major_violations);
        coverage.value = getValue(resource, MetricDefinition.overall_coverage);
        testExecutionTime.value = getValue(resource, MetricDefinition.test_execution_time) / 1_000;
        skippedTests.value = getValue(resource, MetricDefinition.skipped_tests);
    }

    public void setDeltas(Map<MetricDefinition, Double> deltas) {
        lines.delta = deltas.getOrDefault(MetricDefinition.ncloc, 0.0);
        classes.delta = deltas.getOrDefault(MetricDefinition.classes, 0.0);
        classComplexity.delta = deltas.getOrDefault(MetricDefinition.class_complexity, 0.0);
        violations.delta = deltas.getOrDefault(MetricDefinition.blocker_violations, 0.0)
                + deltas.getOrDefault(MetricDefinition.critical_violations, 0.0)
                + deltas.getOrDefault(MetricDefinition.major_violations, 0.0);
        coverage.delta = deltas.getOrDefault(MetricDefinition.overall_coverage, 0.0);
        testExecutionTime.delta = deltas.getOrDefault(MetricDefinition.test_execution_time, 0.0);
        skippedTests.delta = deltas.getOrDefault(MetricDefinition.skipped_tests, 0.0);
    }

    public ProjectMetrics addTo(ProjectMetrics projectMetrics) {
        projectMetrics.lines.value += this.lines.value;
        projectMetrics.lines.delta += this.lines.delta;
        projectMetrics.classes.value += this.classes.value;
        projectMetrics.classes.delta += this.classes.delta;
        projectMetrics.classComplexity.value += this.classComplexity.value;
        projectMetrics.classComplexity.delta += this.classComplexity.delta;
        projectMetrics.violations.value += this.violations.value;
        projectMetrics.violations.delta += this.violations.delta;
        projectMetrics.coverage.value += this.coverage.value;
        projectMetrics.coverage.delta += this.coverage.delta;
        projectMetrics.testExecutionTime.value += this.testExecutionTime.value;
        projectMetrics.testExecutionTime.delta += this.testExecutionTime.delta;
        projectMetrics.skippedTests.value *= this.skippedTests.value;
        projectMetrics.skippedTests.delta += this.skippedTests.delta;
        return projectMetrics;
    }

    @Getter
    @Setter
    public static class ProjectMetric {

        private final DecimalFormat valueFormatter;

        private final DecimalFormat deltaFormatter;

        private double value;

        private double delta;

        public ProjectMetric(String formatValuePattern) {
            this(formatValuePattern, String.format("(+%s);(-%s)", formatValuePattern, formatValuePattern));
        }

        public ProjectMetric(String formatValuePattern, String formatDeltaPattern) {
            valueFormatter = new DecimalFormat(formatValuePattern);
            deltaFormatter = new DecimalFormat(formatDeltaPattern);
        }

        public String getFormattedValue() {
            return valueFormatter.format(value);
        }

        public String getFormattedDelta() {
            return deltaFormatter.format(delta);
        }
    }
}
