package ru.ifmo.rain.hakimov.student;

import info.kgeorgiy.java.advanced.student.Group;
import info.kgeorgiy.java.advanced.student.Student;
import info.kgeorgiy.java.advanced.student.StudentGroupQuery;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class StudentDB implements StudentGroupQuery {
    private static final Comparator<Student> STUDENT_BY_NAME_COMPARATOR = Comparator
            .comparing(Student::getLastName)
            .thenComparing(Student::getFirstName)
            .thenComparingInt(Student::getId);

    private <E> List<E> getBy(List<Student> students, Function<Student, E> mapperFunction) {
        return students
                .stream()
                .map(mapperFunction)
                .collect(Collectors.toList());
    }

    private final String EMPTY = "";

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return getBy(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return getBy(students, Student::getLastName);
    }

    @Override
    public List<String> getGroups(List<Student> students) {
        return getBy(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return getBy(students, (Student s) -> String.format("%s %s", s.getFirstName(), s.getLastName()));
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return getBy(students, Student::getFirstName)
                .stream()
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public String getMinStudentFirstName(List<Student> students) {
        return students.isEmpty() ? EMPTY : students
                .stream()
                .min(Student::compareTo).orElseThrow(() -> new IllegalStateException("No students found!"))
                .getFirstName();
    }

    private List<Student> sortStudentsBy(Collection<Student> students, Comparator<Student> comparator) {
        return students
                .stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return sortStudentsBy(students, Student::compareTo);
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return sortStudentsBy(students, STUDENT_BY_NAME_COMPARATOR);
    }

    private <E> List<Student> findStudentsBy(Collection<Student> students,
                                             Function<Student, E> function,
                                             E value) {
        return students
                .stream()
                .filter(s -> Objects.equals(function.apply(s), value))
                .sorted(STUDENT_BY_NAME_COMPARATOR)
                .collect(Collectors.toList());
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String firstName) {
        return findStudentsBy(students, Student::getFirstName, firstName);
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String lastName) {
        return findStudentsBy(students, Student::getLastName, lastName);
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, String group) {
        return findStudentsBy(students, Student::getGroup, group);
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, String group) {
        return students
                .stream()
                .filter(s -> Objects.equals(s.getGroup(), group))
                .collect(Collectors.groupingBy(Student::getLastName))
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, g -> g.getValue().stream()
                        .map(Student::getFirstName)
                        .min(Comparator.naturalOrder()).orElse(EMPTY)));
    }

    private List<Group> getGroupsBy(Collection<Student> students, Comparator<Student> comparator) {
        return students
                .stream()
                .collect(Collectors.groupingBy(Student::getGroup))
                .entrySet().stream().sorted(Map.Entry.comparingByKey())
                .map(entry -> new Group(entry.getKey(), entry.getValue()
                        .stream()
                        .sorted(comparator)
                        .collect(Collectors.toList())))
                .collect(Collectors.toList());
    }

    @Override
    public List<Group> getGroupsByName(Collection<Student> students) {
        return getGroupsBy(students, STUDENT_BY_NAME_COMPARATOR);
    }

    @Override
    public List<Group> getGroupsById(Collection<Student> students) {
        return getGroupsBy(students, Student::compareTo);
    }

    private String getLargestGroupBy(Collection<Student> students,
                                            Function<Map.Entry<String, List<Student>>, Integer> _size) {
        return students.isEmpty() ? EMPTY : students
                .stream()
                .collect(Collectors.groupingBy(Student::getGroup))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .max(Comparator.comparing(_size)).stream()
                .min(Comparator.comparing(entry -> entry.getValue().stream().min(STUDENT_BY_NAME_COMPARATOR)
                    .orElseThrow(() -> new IllegalStateException("No students found in one of the grops!")))
                )
                .orElseThrow(() -> new IllegalStateException("No groups found!"))
                .getKey();
    }

    @Override
    public String getLargestGroup(Collection<Student> students) {
        return getLargestGroupBy(students, entry -> entry.getValue().size());
    }

    @Override
    public String getLargestGroupFirstName(Collection<Student> students) {
        return getLargestGroupBy(students, entry -> (int) entry.getValue().stream()
                .map(Student::getFirstName)
                .distinct()
                .count());
    }
}
