package gr.aueb.cf;

import gr.aueb.cf.model.Course;
import gr.aueb.cf.model.Teacher;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;

import java.util.List;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("teachercoursesPU");
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();

        /**
         * JPQL
         **/

        /*All teachers*/
        String sql = "SELECT t FROM Teacher t";
        TypedQuery<Teacher> query = em.createQuery(sql, Teacher.class);
        List<Teacher> teachers = query.getResultList();

        /*All courses*/
        String sql1 = "SELECT c FROM Course c";
        TypedQuery<Course> query1 = em.createQuery(sql1, Course.class);
        List<Course> courses = query1.getResultList();

        /*Courses taught by a specific teacher*/
        String sql2 = "SELECT c FROM Course c WHERE c.teacher.firstname LIKE :firstname";
        TypedQuery<Course> query2 = em.createQuery(sql2, Course.class);
        query2.setParameter("firstname", "Panagiotis");
        List<Course> coursesBySpecificTeacher = query2.getResultList();

        /*List of teachers and courses they teach*/
        String sql3 = "SELECT t, c.title FROM Teacher t JOIN t.courses c";
        TypedQuery<Object[]> query3 = em.createQuery(sql3, Object[].class);
        List<Object[]> teachersCourseTitles = query3.getResultList();

        /*Teacher teaching specific course*/
        String sql4 = "SELECT t FROM Teacher t JOIN t.courses c WHERE c.title LIKE :title";
        TypedQuery<Teacher> query4 = em.createQuery(sql4, Teacher.class);
        query4.setParameter("title", "Java");
        List<Teacher> teachersSpecificCourse = query4.getResultList();

        /*List of teachers and the number of courses they teach*/
        String sql5 = "SELECT t.firstname, t. lastname, COUNT(c) FROM Teacher t JOIN t.courses c GROUP BY t.firstname, t.lastname";
        TypedQuery<Object[]> query5 = em.createQuery(sql5, Object[].class);
        List<Object[]> teachersCountCourses = query5.getResultList();

        /*List of teachers and the number of courses they teach, even with zero courses*/
        String sql6 = "SELECT t.firstname, t. lastname, COUNT(c) FROM Teacher t LEFT JOIN t.courses c GROUP BY t.firstname, t.lastname";
        TypedQuery<Object[]> query6 = em.createQuery(sql6, Object[].class);
        List<Object[]> teachersCountZeroCourses = query6.getResultList();

        /*Teachers teaching more than one courses*/
        String sql7 = "SELECT t.firstname, t.lastname FROM Teacher t JOIN t.courses c GROUP BY t.firstname, t.lastname HAVING COUNT(c) > 1";
        TypedQuery<Teacher> query7 = em.createQuery(sql7, Teacher.class);
        List<Teacher> teachersMoreThanOneCourse = query7.getResultList();

        /*Teachers & courses they teach ordered by lastname, title*/
        String sql8 = "SELECT t.firstname, t. lastname, c.title FROM Teacher t JOIN t.courses c ORDER BY t.lastname ASC, c.title ASC";
        TypedQuery<Object[]> query8 = em.createQuery(sql8, Object[].class);
        List<Object[]> orderByLastnameTitle = query8.getResultList();

        /* Teachers that do not teach a course*/
        String sql9 = "SELECT t FROM Teacher t LEFT JOIN t.courses c WHERE c IS NULL";
        TypedQuery<Teacher> query9 = em.createQuery(sql9, Teacher.class);
        List<Teacher> teacherWithNoCourse = query9.getResultList();

        /* Select the most popular courses by teachers' count*/
        String sql10 = "SELECT c.title, COUNT(t) FROM Course c JOIN c.teacher t GROUP BY c.title ORDER BY COUNT(t) DESC";
        TypedQuery<Object[]> query10 = em.createQuery(sql10, Object[].class);
        List<Object[]> popularCourses = query10.getResultList();

        /**
         * Criteria API.
         *
         * Better than JPQL for dynamic queries.
         * Provides two main interfaces: 1) CriteriaBuilder, 2) CriteriaQuery<T>
         *
         * CriteriaBuilder provides API defining predicates (boolean expressions in WHERE clauses).
         *
         * CriteriaQuery<T> provides API  for creating queries, entity roots (FROM)
         * returned results,and for adding criteria (WHERE)
         *
         */

        /* Title courses list*/
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<String> queryTitleCourses = cb.createQuery(String.class);
        Root<Course> course = queryTitleCourses.from(Course.class);
        queryTitleCourses.select(course.get("title"));
        List<String> titles = em.createQuery(queryTitleCourses).getResultList();

        /* Teachers with a specific firstname */
        CriteriaQuery<Teacher> queryTeachersByFirstname = cb.createQuery(Teacher.class);
        Root<Teacher> teacher = queryTeachersByFirstname.from(Teacher.class);
        ParameterExpression<String> firstnameParam = cb.parameter(String.class, "firstname");
        queryTeachersByFirstname.select(teacher).where(cb.equal(teacher.get("firstname"), firstnameParam));
        List<Teacher> teachersByFirstname = em.createQuery(queryTeachersByFirstname).setParameter("firstname", "Panagiotis").getResultList();

        /* Courses taught by a specific teacher*/
        CriteriaQuery<Course> queryCourseBySpecificLastname = cb.createQuery(Course.class);
        Root<Course> courseRoot = queryCourseBySpecificLastname.from(Course.class);
        Join<Course, Teacher> teacherJoin = courseRoot.join("teacher");
        ParameterExpression<String> lastnameParam = cb.parameter(String.class, "lastname");
        queryCourseBySpecificLastname.select(courseRoot).where(cb.equal(teacherJoin.get("lastname"), lastnameParam));
        List<Course> coursesBySpecificLastname = em.createQuery(queryCourseBySpecificLastname).setParameter("lastname", "Sevastos").getResultList();

        /* Teachers with more than one courses*/
        CriteriaQuery<Teacher> queryTeachersMoreThanOne = cb.createQuery(Teacher.class);
        Root<Teacher> teacherRoot = queryTeachersMoreThanOne.from(Teacher.class);
        queryTeachersMoreThanOne.select(teacherRoot).where(cb.greaterThan(cb.size(teacherRoot.get("courses")), 1));
        List<Teacher> teachersMoreThanOne = em.createQuery(queryTeachersMoreThanOne).getResultList();

        /* Courses list including title, lastname, firstname*/
        CriteriaQuery<Object[]> queryCourseTeacher = cb.createQuery(Object[].class);
        Root<Course> cRoot = queryCourseTeacher.from(Course.class);
        Join<Course, Teacher> tJoin = cRoot.join("teacher");
        queryCourseTeacher.multiselect(cRoot.get("title"), tJoin.get("lastname"), tJoin.get("firstname"));
        List<Object[]> coursesTeachers = em.createQuery(queryCourseTeacher).getResultList();

        /* Teachers that do not teach a course*/
        CriteriaQuery<Teacher> queryTeacherNoCourse = cb.createQuery(Teacher.class);
        Root<Teacher> teacherR = queryTeacherNoCourse.from(Teacher.class);
        //Indirect Join
        queryTeacherNoCourse.select(teacherR).where(cb.isEmpty(teacherR.get("courses")));
        List<Teacher> teachersNoCourse = em.createQuery(queryTeacherNoCourse).getResultList();

        /*List of teachers and the number of courses they teach*/
        CriteriaQuery<Object[]> queryTeacherCourses = cb.createQuery(Object[].class);
        Root<Teacher> rootTeacher = queryTeacherCourses.from(Teacher.class);
        Join<Teacher, Course> joinCourse = rootTeacher.join("courses", JoinType.LEFT);
        queryTeacherCourses.multiselect(rootTeacher.get("firstname"), rootTeacher.get("lastname"), cb.count(joinCourse)).groupBy(rootTeacher.get("firstname"),rootTeacher.get("lastname"));
        List<Object[]> TeacherCourses = em.createQuery(queryTeacherCourses).getResultList();

        /*Teacher teaching specific course*/
        CriteriaQuery<Teacher> queryTeacherSpecificCourse = cb.createQuery(Teacher.class);
        Root<Teacher> rootT = queryTeacherSpecificCourse.from(Teacher.class);
        //Join<Teacher, Course> joinC = rootT.join("courses", JoinType.LEFT);
        queryTeacherSpecificCourse.select(rootT).where(cb.equal(rootT.get("courses").get("title"), "SQL"));
        List<Teacher> teacherSpecificCourse = em.createQuery(queryTeacherSpecificCourse).getResultList();

        em.getTransaction().commit();

        //teachers.forEach(System.out::println);
        //courses.forEach(System.out::println);
        //coursesBySpecificTeacher.forEach(System.out::println);

//        for(Object[] row : teachersCourseTitles) {
//            for (Object col : row) {
//                System.out.print(col + " ");
//            }
//            System.out.println();
//        }

        //teachersSpecificCourse.forEach(System.out::println);

//        for(Object[] row : teachersCountCourses) {
//            for (Object col : row) {
//                System.out.print(col + " ");
//            }
//            System.out.println();
//        }

//        for(Object[] row : teachersCountZeroCourses) {
//            for (Object col : row) {
//                System.out.print(col + " ");
//            }
//            System.out.println();
//        }

        //teachersMoreThanOneCourse.forEach(System.out::println);

//        for(Object[] row : orderByLastnameTitle) {
//            for(Object col : row) {
//                System.out.print(col + " ");
//            }
//            System.out.println();
//        }

       // teacherWithNoCourse.forEach(System.out::println);


//        for(Object[] row : TeacherCourses) {
//            for(Object col : row) {
//                System.out.print(col + " ");
//            }
//            System.out.println();
//        }

        teacherSpecificCourse.forEach(System.out::println);

        emf.close();
        em.close();


    }
}
