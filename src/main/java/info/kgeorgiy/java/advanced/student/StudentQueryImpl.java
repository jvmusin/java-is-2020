package info.kgeorgiy.java.advanced.student;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class StudentQueryImpl implements StudentQuery {
    private static final Comparator<Student> STUDENT_COMPARATOR = comparing(Student::getLastName)
            .thenComparing(Student::getFirstName)
            .thenComparingInt(Student::getId);

    private static final Function<Student, String> GET_FULL_NAME = s -> s.getFirstName() + " " + s.getLastName();

    private static <T> List<T> map(Collection<Student> students, Function<Student, T> mapper) {
        return students.stream()
                .map(mapper)
                .collect(toList());
    }

    private static <T extends Comparable<T>> List<Student> sorted(Collection<Student> students, Function<Student, T> getProperty) {
        return students.stream()
                .sorted(comparing(getProperty))
                .collect(toList());
    }

    private static <T> List<Student> filterByPropertyAndSort(Collection<Student> students, Function<Student, T> getProperty, T needValue) {
        return students.stream()
                .filter(s -> getProperty.apply(s).equals(needValue))
                .sorted(STUDENT_COMPARATOR)
                .collect(toList());
    }

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return map(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return map(students, Student::getLastName);
    }

    @Override
    public List<String> getGroups(List<Student> students) {
        return map(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return map(students, GET_FULL_NAME);
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return new HashSet<>(getFirstNames(students));
    }

    @Override
    public String getMinStudentFirstName(List<Student> students) {
        return map(sorted(students, Student::getId), Student::getFirstName)
                .stream()
                .findFirst()
                .orElse("");
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return sorted(students, Student::getId);
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return students.stream().
                sorted(STUDENT_COMPARATOR)
                .collect(toList());
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return filterByPropertyAndSort(students, Student::getFirstName, name);
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return filterByPropertyAndSort(students, Student::getLastName, name);
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, String group) {
        return filterByPropertyAndSort(students, Student::getGroup, group);
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, String group) {
        return findStudentsByGroup(students, group).stream()
                .collect(toMap(
                        Student::getLastName,
                        Student::getFirstName,
                        BinaryOperator.minBy(Comparable::compareTo)
                ));
    }
}
