public class Shared {
	public String sharedString;
	public String clientString;
	// public String serverString;

	public void shared(int n) {
		int local = 2 * n;
		System.out.println(sharedString + local);
	}

	public void client(int n) {
		int local = 2 * n;
		System.out.println(clientString + local);
	}

	/* public void server(int n) {
		int local = 2 * n;
		System.out.println(serverString + local);
	} */
}