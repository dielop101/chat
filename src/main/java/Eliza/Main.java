package Eliza;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		ElizaMain e = new ElizaMain();
		e.readScript(false, "/script");
		String name = "";
		String room_address = "";
		try {
			name = args[0].toString();
			room_address = args[1].toString();
			System.out.println(args[0].toString());
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		e.runProgram("what's up?", null);
		e.runProgram("hello", null);
		e.runProgram("bye", null);

	}

}
