public class Server {
	public String serverString;

	public void foo(int n) {
		int local = 2 * n;
		System.out.println(serverString + local);
	}
}