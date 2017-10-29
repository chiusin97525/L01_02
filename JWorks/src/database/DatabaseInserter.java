package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import exceptions.DatabaseInsertException;

public class DatabaseInserter {

  /**
   * Inserts a problem into the database.
   * @param type An integer used to represent the questions type.
   * @param question The text that would be prompted to the student.
   * @param answer The answer to the question.
   * @param connection The connection to the database.
   * @return The unique key of the problem, -1 if an uncaught error occurred.
   * @throws DatabaseInsertException Thrown if the question could not be added to the database.
   */
  protected static int insertProblem(int type, String question, String answer, Connection connection)
      throws DatabaseInsertException {
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
   * @param connection The connection to the database file.
   * @return The unique ID of the problem set, -1 if an uncaught error occurred.
   * @throws DatabaseInsertException Thrown if the problem set could not be added to the database.
   */
  protected static int insertProblemSet(int maxAttempts, int[] problemIDs, Connection connection) 
      throws DatabaseInsertException {
    String problemsFormatted = "";
    
    for (int problemID : problemIDs) {
      problemsFormatted += Integer.toString(problemID) + ";";
    }
    
    String sql = "INSERT INTO PROBLEMSETS(MAXATTEMPTS, PROBLEMS) VALUES(?,?)";
    int result = -1;
    
    PreparedStatement preparedStatement = null;
    try {
      preparedStatement = connection.prepareStatement(sql, 
          Statement.RETURN_GENERATED_KEYS);
      preparedStatement.setInt(1, maxAttempts);
      preparedStatement.setString(2, problemsFormatted);
    
      int id = 0;
      id = preparedStatement.executeUpdate();
      
      if (id > 0) {
        ResultSet uniqueKey = null;
        uniqueKey = preparedStatement.getGeneratedKeys();
        if (uniqueKey.next()) {
          result = uniqueKey.getInt(1);
          boolean alterResult = 
              DatabaseAlterer.addProblemSetToAttemptsRemaining(result, connection);
          
          preparedStatement.close();
          
          if (!alterResult) {
            result = -1;
          } else {
            boolean initializeResult = insertProblemSetsInitialAttemptCount(result, maxAttempts,
                connection);
            
            if (!initializeResult) {
              result = -1;
            }
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
   * @param studentNumber The unique ID of the student.
   * @param name The name of the student.
   * @param email The email of the student.
   * @param connection The connection to the database file.
   * @return True if the student was added, false if an uncaught error occurred.
   * @throws DatabaseInsertException Thrown if the student could not be added to the database.
   */
  protected static boolean insertStudent(int studentNumber, String name, String email,
      Connection connection) throws DatabaseInsertException {
    String sql = "INSERT INTO STUDENTS(STUDENTNUMBER, NAME, EMAIL) VALUES(?,?,?)";
    boolean result = false;
    
    PreparedStatement preparedStatement = null;
    try {
      preparedStatement = connection.prepareStatement(sql);
      preparedStatement.setInt(1, studentNumber);
      preparedStatement.setString(2, name);
      preparedStatement.setString(3, email);
    
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
   * For each student, gives them the initial attempt count for the given problem set.
   * @param problemSetKey The unique key of the problem set.
   * @param connection The connection to the database file.
   * @return True if the initial attempt counts were added, false otherwise.
   */
  private static boolean insertProblemSetsInitialAttemptCount(int problemSetKey, int maxAttempts,
      Connection connection) {
    
    String sql = "SELECT STUDENTNUMBER FROM STUDENTS";
    boolean result = false;
    
    Statement statement = null;
    
    try {
      statement = connection.createStatement();
      ResultSet studentNumbers = statement.executeQuery(sql);
      
      result = true;
      
      while (studentNumbers.next() & result) {
        int studentNumber = studentNumbers.getInt(1);
        result = DatabaseUpdater.updateAttemptsRemaining(problemSetKey, studentNumber, maxAttempts,
            connection);
      }
      
      statement.close();
    } catch (SQLException e) {
      e.printStackTrace();
      String errorMessage = "Failed to insert inital attempt count for new problem set.";
      System.out.println(errorMessage);
    }
    
    return result;
  }
  
  /**
   * Initializes the amount of attempts a student has for each problem set.
   * @param studentNumber The unique number of the student.
   * @param connection The connection to the database file.
   * @return True if the attempt counts were added for the student, false if an uncaught error
   *         occurs.
   */
  private static boolean insertStudentsInitialAttemptCount(int studentNumber,
      Connection connection) {
    
    String sql = "INSERT INTO ATTEMPTSREMAINING(STUDENTNUMBER) Values(?)";
    boolean result = false;
    
    PreparedStatement preparedStatement = null;
    
    try {
      preparedStatement = connection.prepareStatement(sql);
      preparedStatement.setInt(1, studentNumber);
      preparedStatement.executeUpdate();
      preparedStatement.close();

      sql = "SELECT MAXATTEMPTS FROM PROBLEMSETS";
      Statement statement = connection.createStatement();
      ResultSet maxAttempts = statement.executeQuery(sql);
      
      int count = 1;

      result = true;
      
      while (maxAttempts.next() & result) {
        int problemSetKey = count;
        int attemptsRemaining = maxAttempts.getInt(1);
        
        result = DatabaseUpdater.updateAttemptsRemaining(problemSetKey, studentNumber,
            attemptsRemaining, connection);
        
        count++;
      }

      statement.close();
    } catch (SQLException e) {
      e.printStackTrace();
      String errorMessage = "Failed to insert inital attempt count for new student.";
      System.out.println(errorMessage);
    }
    
    return result;
  }
}