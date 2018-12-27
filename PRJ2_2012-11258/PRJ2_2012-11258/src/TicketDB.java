import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Scanner;

public class TicketDB {

	// define menu string
	static String menuString = 
			"============================================================\r\n" + 
			"1. print all buildings\r\n" + 
			"2. print all performances\r\n" + 
			"3. print all audiences\r\n" + 
			"4. insert a new building\r\n" + 
			"5. remove a building\r\n" + 
			"6. insert a new performance\r\n" + 
			"7. remove a performance\r\n" + 
			"8. insert a new audience\r\n" + 
			"9. remove an audience\r\n" + 
			"10. assign a performance to a building\r\n" + 
			"11. book a performance\r\n" + 
			"12. print all performances which assigned at a building\r\n" + 
			"13. print all audiences who booked for a performance\r\n" + 
			"14. print ticket booking status of a performance\r\n" + 
			"15. exit\r\n" + 
			"============================================================\r\n";
	static Scanner scanner = new Scanner(System.in);
	
	public static void main(String[] args) throws SQLException 
	{
		// initialize connection information
		String serverName = "147.46.15.147";
		String dbName = "db2012-11258";
		String userName = "u2012-11258";
		String password = "778bd0c2fbbd"; //moon quiz사이트 참조
		String url = "jdbc:mariadb://" + serverName + "/" + dbName;
		Connection conn = DriverManager.getConnection(url, userName, password);
		//port는 default 3306이라 지정할 필요 없습니다.

		int actionInput = 0;
		
		System.out.print(menuString);
		while(true)
		{
			System.out.print("Select your action: ");
			try 
			{
				// get input from user
				actionInput = Integer.parseInt(scanner.nextLine());
			}
			catch (NumberFormatException e)
			{
				// if user put non-integer, print error
				actionInput = 0;
			}
			
			switch(actionInput)
		    {
			// handle request with case statement
		        case 1:
		        	printAllBuildings(conn);
		        	break;
		        case 2:
		        	printAllPerformances(conn);
		        	break;
		        case 3:
		        	printAllAudiences(conn);
		        	break;
		        case 4:
		        	insertNewBuilding(conn);
		        	break;
		        case 5:
		        	removeBuilding(conn);
		        	break;
		        case 6:
		        	insertNewPerformance(conn);
		        	break;
		        case 7:
		        	removePerformance(conn);
		        	break;
		        case 8:
		        	insertNewAudience(conn);
		        	break;
		        case 9:
		        	removeAudience(conn);
		        	break;
		        case 10:
		        	assignPerformance(conn);
		        	break;
		        case 11:
		        	bookPerformance(conn);
		        	break;
		        case 12:
		        	printAllPerformancesAssignedAtBuilding(conn);
		        	break;
		        case 13:
		        	printAllAudiencesBookedForPerformance(conn);
		        	break;
		        case 14:
		        	printBookingStatusOfPerformance(conn);
		        	break;
		        case 15:
		        	System.out.println("Bye!");
		        	return;
		        default: 
		        	System.out.println("Invalid action");
		        	System.out.println();
		        // if user put wrong integer, print error
		    }
		}

	}
	
	// function that print ticket booking status of a performance
	private static void printBookingStatusOfPerformance(Connection conn) throws SQLException 
	{
		// first, check the performance exist
		int capacity;
		String sqlS = "select * from performance where id = ?;";
		PreparedStatement stmtS = conn.prepareStatement(sqlS);
		System.out.print("Performance ID: ");
		String performance_id = scanner.nextLine();
		
		//execute query
		stmtS.setString(1, performance_id);
		ResultSet rsS = stmtS.executeQuery();
		if(!rsS.next()) // if performance doesn't exist
		{
			System.out.println("Performance "+ performance_id +" doesn't exist");
			System.out.println();
			return;
		}
		
		// next, check the performance assigned and get capacity.
		String sqlC = "select * from assignment, building where assignment.building_id = building.id and performance_id = ?;";
		PreparedStatement stmtC = conn.prepareStatement(sqlC);
		
		//execute query
		stmtC.setString(1, performance_id);
		ResultSet rsC = stmtC.executeQuery();
		if(!rsC.next()) // if performance isn't assigned
		{
			System.out.println("Performance "+ performance_id +" isn't assigned");
			System.out.println();
			return;
		}
		else
		{
			capacity = Integer.parseInt(rsC.getString("capacity"));
		}
		
		// finally execute print query
		String sql = "select seat_number, audience_id from booking where performance_id = ? order by seat_number;";
		PreparedStatement stmt = conn.prepareStatement(sql);
		stmt.setString(1, performance_id);
		ResultSet rs = stmt.executeQuery();
		
		System.out.println("--------------------------------------------------------------------------------");
		System.out.printf("%-40s%-40s\n", "seat_number","audience_id");
		System.out.println("--------------------------------------------------------------------------------");
		for(int i=1; i<=capacity; i++) // traverse all seat including unsold seat
		{
			if(rs.next())
			{
				String seat_number = rs.getString("seat_number");
				String audience_id = rs.getString("audience_id");
				
				for(;i<Integer.parseInt(seat_number);i++)
				{
					System.out.printf("%-40s%-40s\n", i,"");
				}
				System.out.printf("%-40s%-40s\n", seat_number,audience_id);
			}
			else
			{
				System.out.printf("%-40s%-40s\n", i,"");
			}
		}
		System.out.println("--------------------------------------------------------------------------------\n");
	}

	// function that print all audiences who booked for a performance
	private static void printAllAudiencesBookedForPerformance(Connection conn) throws SQLException 
	{
		// first, check the performance exist
		String sqlS = "select * from performance where id = ?;";
		PreparedStatement stmtS = conn.prepareStatement(sqlS);
		System.out.print("Performance ID: ");
		String performance_id = scanner.nextLine();
		
		//execute query
		stmtS.setString(1, performance_id);
		ResultSet rsS = stmtS.executeQuery();
		if(!rsS.next()) // if performance doesn't exist
		{
			System.out.println("Performance "+ performance_id +" doesn't exist");
			System.out.println();
			return;
		}
		
		// next, execute print query.
		
		String sql = "select DISTINCT id, name, gender, age from audience, booking where audience.id = booking.audience_id and booking.performance_id = ? order by id;";
		PreparedStatement stmt = conn.prepareStatement(sql);
		stmt.setString(1, performance_id);
		ResultSet rs = stmt.executeQuery();
		
		System.out.println("--------------------------------------------------------------------------------");
		System.out.printf("%-8s%-40s%-16s%-16s\n", "id","name","gender","age");
		System.out.println("--------------------------------------------------------------------------------");
		while(rs.next()) 
		{
			String id = rs.getString("id");
			String name = rs.getString("name");
			String gender = rs.getString("gender");
			String age = rs.getString("age");
			System.out.printf("%-8s%-40s%-16s%-16s\n", id,name,gender,age);
		}
		System.out.println("--------------------------------------------------------------------------------\n");
	}

	// function that print all performances assigned at a building
	private static void printAllPerformancesAssignedAtBuilding(Connection conn) throws SQLException 
	{
		// first, check the building exist
		String sqlS = "select * from building where id = ?;";
		PreparedStatement stmtS = conn.prepareStatement(sqlS);
		System.out.print("Building ID: ");
		String building_id = scanner.nextLine();
		
		//execute query
		stmtS.setString(1, building_id);
		ResultSet rsS = stmtS.executeQuery();
		if(!rsS.next()) // if building doesn't exist
		{
			System.out.println("Building "+ building_id +" doesn't exist");
			System.out.println();
			return;
		}
		
		// next, execute print query.
		
		String sql = "select a.id, a.name, a.type, a.price, ifnull(b.booked,0) as booked from \r\n" + 
				"(select * from performance, assignment where performance.id = assignment.performance_id and assignment.building_id = ?) a \r\n" + 
				"left outer join\r\n" + 
				"(select performance_id, count(seat_number) as booked\r\n" + 
				"from booking group by (performance_id)) b\r\n" + 
				"on (a.id = b.performance_id)\r\n" + 
				"order by id;";
		PreparedStatement stmt = conn.prepareStatement(sql);
		stmt.setString(1, building_id);
		ResultSet rs = stmt.executeQuery();
		
		System.out.println("--------------------------------------------------------------------------------");
		System.out.printf("%-8s%-32s%-16s%-16s%-8s\n", "id","name","type","price","booked");
		System.out.println("--------------------------------------------------------------------------------");
		while(rs.next()) 
		{
			String id = rs.getString("id");
			String name = rs.getString("name");
			String type = rs.getString("type");
			String price = rs.getString("price");
			String booked = rs.getString("booked");
			System.out.printf("%-8s%-32s%-16s%-16s%-8s\n", id,name,type,price,booked);
		}
		System.out.println("--------------------------------------------------------------------------------\n");
	}

	// function that book a performance
	private static void bookPerformance(Connection conn) throws SQLException 
	{
		// first, get capacity and price of performance with select query
		int capacity, price;
		String sqlS = "select building.capacity as capacity, performance.price as price from building, performance, (select building_id, performance_id from assignment where performance_id = ?) a\r\n" + 
				"where building.id = a.building_id and performance.id = a.performance_id;";
		PreparedStatement stmtS = conn.prepareStatement(sqlS);

		System.out.print("Performance ID: ");
		String performance_id = scanner.nextLine();
		
		stmtS.setString(1, performance_id);
		ResultSet rs = stmtS.executeQuery();
		
		if(rs.next()) 
		{
			capacity = Integer.parseInt(rs.getString("capacity"));
			price = Integer.parseInt(rs.getString("price"));
		}
		else // if input performance id is not in assignment table
		{
			System.out.println("Performance "+ performance_id +" isn't assigned");
			System.out.println();
			return;
		}
		
		// second, get age of audience with select query
		int age;
		String sqlA = "select age from audience where id = ?;";
		PreparedStatement stmtA = conn.prepareStatement(sqlA);
		
		System.out.print("Audience ID: ");
		String audience_id = scanner.nextLine();
		
		stmtA.setString(1, audience_id);
		ResultSet rsA = stmtA.executeQuery();
		
		if(rsA.next()) 
		{
			age = Integer.parseInt(rsA.getString("age"));
		}
		else // if input audience_id is not in audience table
		{
			System.out.println("There is no such id in audience table"); // additional error check
			System.out.println();
			return;
		}
		
		
		System.out.print("Seat number: ");
		String[] seatNumberArray = scanner.nextLine().replaceAll(" ", "").split(",");
		
		// next, check all seat_numbers are acceptable
		
		for(String seat_number : seatNumberArray)
		{
			String sqlC = "select * from booking where performance_id = ? and seat_number = ?;";
			PreparedStatement stmtC = conn.prepareStatement(sqlC);
			
			stmtC.setString(1, performance_id);
			stmtC.setString(2, seat_number);
			ResultSet rsC = stmtC.executeQuery();
			
			//check seat_number range
			try 
			{
				if(Integer.parseInt(seat_number) < 1 || Integer.parseInt(seat_number) > capacity)
				{
					System.out.println("Seat number out of range");
					System.out.println();
					return;
				}
			}
			catch(NumberFormatException e) // additional error check : seat_number value exceed limit of integer
			{
				System.out.println("Seat number exceed limit of integer");
				System.out.println();
				return;
			}
			
			if(rsC.next()) // if booking table already have same seat_number with performance_id, occur error
			{
				System.out.println("The seat is already taken");
				System.out.println();
				return;
			}
		}
		
		// finally, insert booking informations
		
		double count = 0;
		double discountedPrice;
		if(age < 8)
			discountedPrice = 0;
		else if(age < 13)
			discountedPrice = price * 0.5;
		else if(age < 19)
			discountedPrice = price * 0.8;
		else
			discountedPrice = price;
		
		for(String seat_number : seatNumberArray)
		{
			String sql = "INSERT INTO booking (performance_id, audience_id, seat_number) VALUES (?, ?, ?);";
			PreparedStatement stmt = conn.prepareStatement(sql);
			
			//execute query
			stmt.setString(1, performance_id);
			stmt.setString(2, audience_id);
			stmt.setString(3, seat_number);
			try
			{
				stmt.executeUpdate();
			}
			catch (SQLIntegrityConstraintViolationException e)
			{
				 // unexpected error catch : code cannot reach here
				System.out.println("Unexpected error occur in assigning performance");
				System.out.println();
				return;
			}
			count++;
		}
		
		int totalPrice = (int) Math.round(count * discountedPrice);
		
		System.out.println("Successfully book a performance\r\n" + 
				"Total ticket price is "+ totalPrice);
		System.out.println();

	}

	// function that assign a performance to a building
	private static void assignPerformance(Connection conn) throws SQLException 
	{
		String sql = "INSERT INTO assignment (building_id, performance_id) VALUES (?, ?);";
		PreparedStatement stmt = conn.prepareStatement(sql);

		System.out.print("Building ID: ");
		String building_id = scanner.nextLine();
		System.out.print("Performance ID: ");
		String performance_id = scanner.nextLine();
		
		//execute query
		stmt.setString(1, building_id);
		stmt.setString(2, performance_id);
		try
		{
			stmt.executeUpdate();
		}
		catch (SQLIntegrityConstraintViolationException e) // check constraints
		{
			if(e.getErrorCode() == 1062) // primary key constraint violation : duplicate performance assignment
			{
				System.out.println("Performance "+ performance_id +" is already assigned");
				System.out.println();
				return;
			}
			else if(e.getErrorCode() == 1452) // foreign key constraint violation : there is no such performance_id or building_id
			{
				System.out.println("There is no such id in building table or performance table");
				System.out.println();
				return;
			}
			else // unexpected error catch : code cannot reach here
			{
				System.out.println("Unexpected error occur in assigning performance");
				System.out.println();
				return;
			}
		}
		System.out.println("Successfully assign a performance");
		System.out.println();
	}

	// function that remove an audience
	private static void removeAudience(Connection conn) throws SQLException 
	{
		String sql = "DELETE FROM audience WHERE id = ?;";
		PreparedStatement stmt = conn.prepareStatement(sql);

		System.out.print("Audience ID: ");
		String id = scanner.nextLine();
		
		//execute query
		stmt.setString(1, id);
		
		int success = stmt.executeUpdate();
		
		if(success == 0) // if result of executeUpdate is 0, it means that there is no row that match with input id
		{
			System.out.println("Audience "+ id +" doesn't exist");
			System.out.println();
		}
		else
		{
			System.out.println("An audience is successfully removed");
			System.out.println();
		}
	}

	// function that insert a new audience
	private static void insertNewAudience(Connection conn) throws SQLException 
	{
		String sql = "INSERT INTO audience (name, gender, age) VALUES (?, ?, ?);";
		PreparedStatement stmt = conn.prepareStatement(sql);

		System.out.print("Audience name: ");
		String name = scanner.nextLine();
		System.out.print("Audience gender: ");
		String gender = scanner.nextLine();
		//check gender
		if(!(gender.equals("M")||gender.equals("F")))
		{
			System.out.println("Gender should be 'M' or 'F'");
			System.out.println();
			return;
		}
		System.out.print("Audience age: ");
		String age = scanner.nextLine();

		//truncate
		if(name.length() > 200)
			name = name.substring(0,200);
	
		//check age range
		try 
		{
			if(Integer.parseInt(age) < 1)
			{
				System.out.println("Age should be more than 0");
				System.out.println();
				return;
			}
		}
		catch(NumberFormatException e) // additional error check : age value exceed limit of integer
		{
			System.out.println("Age exceed limit of integer");
			System.out.println();
			return;
		}
		
		//execute query
		stmt.setString(1, name);
		stmt.setString(2, gender);
		stmt.setString(3, age);
		stmt.executeUpdate();
		
		System.out.println("An audience is successfully inserted");
		System.out.println();
	}

	// function that remove a performance
	private static void removePerformance(Connection conn) throws SQLException 
	{
		String sql = "DELETE FROM performance WHERE id = ?;";
		PreparedStatement stmt = conn.prepareStatement(sql);

		System.out.print("Performance ID: ");
		String id = scanner.nextLine();
		
		//execute query
		stmt.setString(1, id);
		
		int success = stmt.executeUpdate();
		
		if(success == 0) // if result of executeUpdate is 0, it means that there is no row that match with input id
		{
			System.out.println("Performance "+ id +" doesn't exist");
			System.out.println();
		}
		else
		{
			System.out.println("A performance is successfully removed");
			System.out.println();
		}
	}
	
	// function that insert a new performance
	private static void insertNewPerformance(Connection conn) throws SQLException 
	{
		String sql = "INSERT INTO performance (name, type, price) VALUES (?, ?, ?);";
		PreparedStatement stmt = conn.prepareStatement(sql);

		System.out.print("Performance name: ");
		String name = scanner.nextLine();
		System.out.print("Performance type: ");
		String type = scanner.nextLine();
		System.out.print("Performance price: ");
		String price = scanner.nextLine();

		//truncate
		if(name.length() > 200)
			name = name.substring(0,200);
		if(type.length() > 200)
			type = type.substring(0,200);
		
		//check price range
		try 
		{
			if(Integer.parseInt(price) < 0)
			{
				System.out.println("Price should be 0 or more");
				System.out.println();
				return;
			}
		}
		catch(NumberFormatException e) // additional error check : price value exceed limit of integer
		{
			System.out.println("Price exceed limit of integer");
			System.out.println();
			return;
		}
		
		//execute query
		stmt.setString(1, name);
		stmt.setString(2, type);
		stmt.setString(3, price);
		stmt.executeUpdate();
		
		System.out.println("A performance is successfully inserted");
		System.out.println();
	}

	// function that remove a building
	private static void removeBuilding(Connection conn) throws SQLException 
	{
		String sql = "DELETE FROM building WHERE id = ?;";
		PreparedStatement stmt = conn.prepareStatement(sql);

		System.out.print("Building ID: ");
		String id = scanner.nextLine();
		
		//execute query
		stmt.setString(1, id);
		
		int success = stmt.executeUpdate();
		
		if(success == 0) // if result of executeUpdate is 0, it means that there is no row that match with input id
		{
			System.out.println("Building "+ id +" doesn't exist");
			System.out.println();
		}
		else
		{
			System.out.println("A building is successfully removed");
			System.out.println();
		}
	}

	// function that insert a new building
	private static void insertNewBuilding(Connection conn) throws SQLException 
	{
		String sql = "INSERT INTO building (name, location, capacity) VALUES (?, ?, ?);";
		PreparedStatement stmt = conn.prepareStatement(sql);

		System.out.print("Building name: ");
		String name = scanner.nextLine();
		System.out.print("Building location: ");
		String location = scanner.nextLine();
		System.out.print("Building capacity: ");
		String capacity = scanner.nextLine();

		//truncate
		if(name.length() > 200)
			name = name.substring(0,200);
		if(location.length() > 200)
			location = location.substring(0,200);
		
		//check capacity range
		try 
		{
			if(Integer.parseInt(capacity) < 1)
			{
				System.out.println("Capacity should be larger than 0");
				System.out.println();
				return;
			}
		}
		catch(NumberFormatException e) // additional error check : capacity value exceed limit of integer
		{
			System.out.println("Capacity exceed limit of integer");
			System.out.println();
			return;
		}
		
		//execute query
		stmt.setString(1, name);
		stmt.setString(2, location);
		stmt.setString(3, capacity);
		stmt.executeUpdate();
		
		System.out.println("A building is successfully inserted");
		System.out.println();
	}

	// function that print all audiences.
	private static void printAllAudiences(Connection conn) throws SQLException 
	{
		String sql = "select * from audience order by id;";
		PreparedStatement stmt = conn.prepareStatement(sql);
		ResultSet rs = stmt.executeQuery();
		
		System.out.println("--------------------------------------------------------------------------------");
		System.out.printf("%-8s%-40s%-16s%-16s\n", "id","name","gender","age");
		System.out.println("--------------------------------------------------------------------------------");
		while(rs.next()) 
		{
			String id = rs.getString("id");
			String name = rs.getString("name");
			String gender = rs.getString("gender");
			String age = rs.getString("age");
			System.out.printf("%-8s%-40s%-16s%-16s\n", id,name,gender,age);
		}
		System.out.println("--------------------------------------------------------------------------------\n");
	}

	//function that print all performances.
	private static void printAllPerformances(Connection conn) throws SQLException 
	{
		String sql = "select a.id, a.name, a.type, a.price, ifnull(b.booked,0) as booked from\r\n" + 
				"(select * from performance) a\r\n" + 
				"left outer join \r\n" + 
				"(select performance_id, count(seat_number) as booked \r\n" + 
				"from booking group by (performance_id)) b\r\n" + 
				"on (a.id = b.performance_id)\r\n" + 
				"order by id;";
		PreparedStatement stmt = conn.prepareStatement(sql);
		ResultSet rs = stmt.executeQuery();
		
		System.out.println("--------------------------------------------------------------------------------");
		System.out.printf("%-8s%-32s%-16s%-16s%-8s\n", "id","name","type","price","booked");
		System.out.println("--------------------------------------------------------------------------------");
		while(rs.next()) 
		{
			String id = rs.getString("id");
			String name = rs.getString("name");
			String type = rs.getString("type");
			String price = rs.getString("price");
			String booked = rs.getString("booked");
			System.out.printf("%-8s%-32s%-16s%-16s%-8s\n", id,name,type,price,booked);
		}
		System.out.println("--------------------------------------------------------------------------------\n");
	}

	// function that print all buildings.
	private static void printAllBuildings(Connection conn) throws SQLException 
	{
		String sql = "select a.id, a.name, a.location, a.capacity, ifnull(b.assigned,0) as assigned from\r\n" + 
				"(select * from building) a\r\n" + 
				"left outer join \r\n" + 
				"(select building_id, count(performance_id) as assigned \r\n" + 
				"from assignment group by (building_id)) b\r\n" + 
				"on (a.id = b.building_id)\r\n" + 
				"order by id;";
		PreparedStatement stmt = conn.prepareStatement(sql);
		ResultSet rs = stmt.executeQuery();
		
		System.out.println("--------------------------------------------------------------------------------");
		System.out.printf("%-8s%-32s%-16s%-16s%-8s\n", "id","name","location","capacity","assigned");
		System.out.println("--------------------------------------------------------------------------------");
		while(rs.next()) 
		{
			String id = rs.getString("id");
			String name = rs.getString("name");
			String location = rs.getString("location");
			String capacity = rs.getString("capacity");
			String assigned = rs.getString("assigned");
			System.out.printf("%-8s%-32s%-16s%-16s%-8s\n", id,name,location,capacity,assigned);
		}
		System.out.println("--------------------------------------------------------------------------------\n");
	}
}
