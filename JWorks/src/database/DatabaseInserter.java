package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import exceptions.DatabaseInsertException;

public class DatabaseInserter {

  /**
   * Inserts a problem into the database.
   * @param type An integer used to represent the questions type.
   * @param question The text that would be prompted to the student.
   * @param answer The answer to the question.
   * @param instructorID The unique ID of the instructor who created the problem.
   * @param connection The connection to the database.
   * @return The unique key of the problem, -1 if an uncaught error occurred.
   * @throws DatabaseInsertException Thrown if the question could not be added to the database.
   */
  protected static int insertProblem(int type, String question, String answer, int instructorID,
      Connection connection) throws DatabaseInsertException {
    String sql = "INSERT INTO PROBLEMS(TYPE, QUESTION, ANSWER) VALUES(?,?,?)";
    int result = -1;
    
    PreparedStatement preparedStatement = null;
    try {
        preparedStatement = connection.prepareStatement(sql, 
            Statement.RETURN_GENERATED_KEYS);
        preparedStatement.setInt(1, type);
        preparedStatement.setString(2, question);
        preparedStatement.setString(3, answer);
      
      int id = 0;
      id = preparedStatement.executeUpdate();
      
      if (id > 0) {
        ResultSet uniqueKey = null;
        uniqueKey = preparedStatement.getGeneratedKeys();
        if (uniqueKey.next()) {
          result = uniqueKey.getInt(1);
          
          sql = "INSERT INTO INSTRUCTORS_PROBLEMS_RELATIONSHIP(INSTRUCTOR, PROBLEM) "
              + "VALUES(?,?)";
          
          preparedStatement = connection.prepareStatement(sql);
          preparedStatement.setInt(1, instructorID);
          preparedStatement.setInt(2, result);
          
          preparedStatement.executeUpdate();
        }
        
        preparedStatement.close();
      }
    } catch (SQLException e) {
      String errorMessage = "Failed to insert a problem into the database.";
      throw new DatabaseInsertException(errorMessage);
    }
    
    return result;
  }

  /**
   * Inserts a problem set into the database by collecting an array of numbers.
   * @param maxAttempts The amount of attempts allowed for this problem set, -8 for infinite.
   * @param problemIDs The unique IDs of the problems.
   * @param startTime The time when the problem set is to be released.
   * @param endTime The time when the problem set is due.
   * @param instructorID The unique ID of the instructor who created the problem set.
   * @param connection The connection to the database file.
   * @return The unique ID of the problem set, -1 if an uncaught error occurred.
   * @throws DatabaseInsertException Thrown if the problem set could not be added to the database.
   */
  protected static int insertProblemSet(int maxAttempts, int[] problemIDs, Date startTime,
      Date endTime, int instructorID, Connection connection) throws DatabaseInsertException {
    
    String sql = "INSERT INTO PROBLEMSETS(MAXATTEMPTS, STARTTIME, ENDTIME) VALUES(?,?,?)";
    int result = -1;
    
    PreparedStatement preparedStatement = null;
    try {
      preparedStatement = connection.prepareStatement(sql, 
          Statement.RETURN_GENERATED_KEYS);
      preparedStatement.setInt(1, maxAttempts);
      preparedStatement.setLong(2, startTime.getTime() / 1000L);
      preparedStatement.setLong(3, endTime.getTime() / 1000L);
      
      int id = 0;
      id = preparedStatement.executeUpdate();
      
      // If the ID generated is valid.
      if (id > 0) {
        ResultSet uniqueKey = null;
        uniqueKey = preparedStatement.getGeneratedKeys();
        
        // If there exists a key.
        if (uniqueKey.next()) {
          result = uniqueKey.getInt(1);
          
          boolean initializeResult = insertProblemSetsInitialAttemptCount(result, maxAttempts,
              connection);
          
          // If the initial attempt counts could not be initialized.
          if (!initializeResult) {
            result = -1;
          } else {
            // If everything else worked.
            
            sql = "INSERT INTO PROBLEMSETS_PROBLEMS_RELATIONSHIP(PROBLEMSET, PROBLEM) VALUES (?,?)";
            
            // Adds the problems IDs to the relationship table with the generated ID for the
            // problem set.
            for (int problemID : problemIDs) {
              
              preparedStatement = connection.prepareStatement(sql);
              preparedStatement.setInt(1, result);
              preparedStatement.setInt(2, problemID);
              
              preparedStatement.executeUpdate();
            }
            
            sql = "INSERT INTO INSTRUCTORS_PROBLEMSETS_RELATIONSHIP(INSTRUCTOR, PROBLEMSET)"
                + "VALUES (?,?)";
            
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, instructorID);
            preparedStatement.setInt(2, result);
            
            preparedStatement.executeUpdate();
            
            preparedStatement.close();
          }
        }
      }
    } catch (SQLException e) {
      String errorMessage = "Failed to insert problem set into the database.";
      throw new DatabaseInsertException(errorMessage);
    }
    
    return result;
  }
  
  /**
   * Inserts a student into the database.
   * @param studentNumber The unique ID of the student, as determined by the user.
   * @param name The name of the student.
   * @param email The email of the student.
   * @param password The password created for the student.
   * @param connection The connection to the database file.
   * @return True if the student was added, false if an uncaught error occurred.
   * @throws DatabaseInsertException Thrown if the student could not be added to the database.
   */
  protected static boolean insertStudent(int studentNumber, String name, String email,
      String password, Connection connection) throws DatabaseInsertException {
    String sql = "INSERT INTO STUDENTS(STUDENTNUMBER, NAME, EMAIL, PASSWORD) VALUES(?,?,?,?)";
    boolean result = false;
    
    PreparedStatement preparedStatement = null;
    try {
      preparedStatement = connection.prepareStatement(sql);
      preparedStatement.setInt(1, studentNumber);
      preparedStatement.setString(2, name);
      preparedStatement.setString(3, email);
      preparedStatement.setString(4, password);
    
      preparedStatement.executeUpdate();
      
      preparedStatement.close();
      
      result = insertStudentsInitialAttemptCount(studentNumber, connection);
    } catch (SQLException e) {
      String errorMessage = "Failed to insert student into the database.";
      throw new DatabaseInsertException(errorMessage);
    }
    
    return result;
  }
  
  /**
   * Inserts an instructor into the database.
   * @param instructorNumber The unique ID of the instructor, as determined by the user.
   * @param name The name of the instructor.
   * @param email The email address of the instructor.
   * @param password The password created for the instructor.
   * @param connection The connection to the database file.
   * @return The unique ID of the instructor, -1 if an uncaught error occurs.
   * @throws DatabaseInsertException Thrown if the instructor could not be added to the database.
   */
  protected static boolean insertInstructor(int instructorNumber, String name, String email,
      String password, Connection connection) throws DatabaseInsertException {
    
    String sql = "INSERT INTO INSTRUCTORS(ID, NAME, EMAIL, PASSWORD) VALUES(?,?,?,?)";
    boolean result = false;
    
    PreparedStatement  preparedStatement = null;
    try {
      preparedStatement = connection.prepareStatement(sql);
      preparedStatement.setInt(1, instructorNumber);
      preparedStatement.setString(2, name);
      preparedStatement.setString(3, email);
      preparedStatement.setString(4, password);
      
      preparedStatement.executeUpdate();
      
      preparedStatement.close();
      
      result = true;
      
    } catch (SQLException e) {
      String errorMessage = "Failed to insert instructor into the database.";
      throw new DatabaseInsertException(errorMessage);
    }
    
    return result;
  }
  
  /**
   * Keeps a record of a students attempt for a problem set including the problem set key
   * @param studentNumber The unique ID of the student.
   * @param problemSetKey The unique ID of the problem set.
   * @param time The time when the attempt took place in seconds from the epoch.
   * @param problems The unique IDs of the problems in the problem set.
   * @param answers The student's answers to the problems. This list should correspond directly
   *                with the problems list by index.
   * @param connection The connection to the database file.
   * @return True if the attempt was stored in the database; false indicates that some error
   *         occurred.
   * @throws DatabaseInsertException Thrown if the students attempt could not be stored in the
   *                                 database.
   */
  protected static boolean insertStudentsAttempt(int studentNumber, int problemSetKey, long time,
      int[] problems, String [] answers, Connection connection)
      throws DatabaseInsertException {
    
    boolean result = false;
    String sql = "INSERT INTO PREVIOUSATTEMPTS(STUDENTNUMBER, PROBLEMSET, TIME, PROBLEM, "
        + "STUDENTANSWER) VALUES(?,?,?,?,?)";
    
    PreparedStatement preparedStatement = null;
    
    try {
      preparedStatement = connection.prepareStatement(sql);
      // These values shouldn't change across the different problems.
      preparedStatement.setInt(1, studentNumber);
      preparedStatement.setInt(2, problemSetKey);
      preparedStatement.setLong(3, time);
      
      for (int i = 0; i < problems.length; i++) {
        preparedStatement.setInt(4, problems[i]);
        preparedStatement.setString(5, answers[i]);
        
        preparedStatement.executeUpdate();
      }
      
      preparedStatement.close();
      
      result = true;
    } catch (SQLException e) {
      String errorMessage = "Failed to insert the result of a students attempt.";
      throw new DatabaseInsertException(errorMessage);
    }
    
    return result;
  }
  
  /**
   * Associates the given tag with the given problem in the database.
   * @param problemID The unique ID of the problem.
   * @param tag The tag to be associated with the problem.
   * @param connection The connection to the database file.
   * @return True if the tag was associated with the problem; false indicates an error occurred.
   * @throws DatabaseInsertException Thrown if the tag could not be added to the database.
   */
  protected static boolean insertProblemTag(int problemID, String tag, Connection connection)
      throws DatabaseInsertException {
   
    boolean result = false;
    String sql = "INSERT INTO PROBLEMTAGS(PROBLEM, TAG) VALUES (?,?)";
    
    PreparedStatement preparedStatement = null;
    
    try {
      preparedStatement = connection.prepareStatement(sql);
      
      preparedStatement.setInt(1, problemID);
      preparedStatement.setString(2, tag);
      
      preparedStatement.executeUpdate();
      
      preparedStatement.close();
      
      result = true;
    } catch (SQLException e) {
      String errorMessage = "Failed to insert a problem tag into the database.";
      throw new DatabaseInsertException(errorMessage);
    }
    
    return result;
  }
  
  /**
   * Associates the given tag with the given problem set in the database.
   * @param problemSetID The unique ID of the problem set.
   * @param tag The tag to be associated with the problem set.
   * @param connection The connection to the database file.
   * @return True if the tag was associated with the problem set; false indicates an error occurred.
   * @throws DatabaseInsertException Thrown if the tag could not be added to the database.
   */
  protected static boolean insertProblemSetTag(int problemSetID, String tag, Connection connection)
      throws DatabaseInsertException {
   
    boolean result = false;
    String sql = "INSERT INTO PROBLEMSETTAGS(PROBLEMSET, TAG) VALUES (?,?)";
    
    PreparedStatement preparedStatement = null;
    
    try {
      preparedStatement = connection.prepareStatement(sql);
      
      preparedStatement.setInt(1, problemSetID);
      preparedStatement.setString(2, tag);
      
      preparedStatement.executeUpdate();
      
      preparedStatement.close();
      
      result = true;
    } catch (SQLException e) {
      String errorMessage = "Failed to insert a problem set tag into the database.";
      throw new DatabaseInsertException(errorMessage);
    }
    
    return result;
  }
  
  /**
   * For each student, gives them the initial attempt count for the given problem set.
   * @param problemSetKey The unique key of the problem set.
   * @param connection The connection to the database file.
   * @return True if the initial attempt counts were added, false otherwise.
   * @throws DatabaseInsertException Thrown if the problem sets initial attempt count could not be
   *                                 set for all students.
   */
  private static boolean insertProblemSetsInitialAttemptCount(int problemSetKey, int maxAttempts,
      Connection connection) throws DatabaseInsertException {
    
    String sql = "SELECT STUDENTNUMBER FROM STUDENTS";
    boolean result = false;
    
    Statement statement = null;
    
    try {
      statement = connection.createStatement();
      ResultSet studentNumbers = statement.executeQuery(sql);

      PreparedStatement preparedStatement = null;
      
      sql = "INSERT INTO ATTEMPTSREMAINING(STUDENTNUMBER, PROBLEMSET, ATTEMPTSREMAINING) "
          + "Values(?,?,?)";
      
      preparedStatement = connection.prepareStatement(sql);
      // These values shouldn't change across the different students.
      preparedStatement.setInt(2, problemSetKey);
      preparedStatement.setInt(3, maxAttempts);
      
      while (studentNumbers.next()) {
        int studentNumber = studentNumbers.getInt(1);
        
        preparedStatement.setInt(1, studentNumber);

        preparedStatement.executeUpdate();
      }

      statement.close();
      preparedStatement.close();
      
      result = true;
    } catch (SQLException e) {
      String errorMessage = "Failed to insert inital attempt count for new problem set.";
      throw new DatabaseInsertException(errorMessage);
    }
    
    return result;
  }
  
  /**
   * Initializes the amount of attempts a student has for each problem set.
   * @param studentNumber The unique number of the student.
   * @param connection The connection to the database file.
   * @return True if the attempt counts were added for the student, false if an uncaught error
   *         occurs.
   * @throws DatabaseInsertException Thrown if the initial attempt count for each problem set could
   *                                 not be stored for the student.
   */
  private static boolean insertStudentsInitialAttemptCount(int studentNumber,
      Connection connection) throws DatabaseInsertException {
    
    boolean result = false;

    String sql = "SELECT ID, MAXATTEMPTS FROM PROBLEMSETS";
    
    try {
      Statement statement = connection.createStatement();
      ResultSet problemSetData = statement.executeQuery(sql);

      PreparedStatement preparedStatement = null;
      
      sql = "INSERT INTO ATTEMPTSREMAINING(STUDENTNUMBER, PROBLEMSET, ATTEMPTSREMAINING) "
          + "Values(?,?,?)";
      
      preparedStatement = connection.prepareStatement(sql);
      // This value shoudln't change across the different problem sets.
      preparedStatement.setInt(1, studentNumber);
      
      while (problemSetData.next()) {
        
        int problemSetKey = problemSetData.getInt(1);
        int initialAttemptCount = problemSetData.getInt(2);
        
        
        preparedStatement.setInt(2, problemSetKey);
        preparedStatement.setInt(3, initialAttemptCount);

        preparedStatement.executeUpdate();
      }

      statement.close();
      preparedStatement.close();
      
      result = true;
    } catch (SQLException e) {
      String errorMessage = "Failed to insert inital attempt count for new student.";
      throw new DatabaseInsertException(errorMessage);
    }
    
    return result;
  }

}
