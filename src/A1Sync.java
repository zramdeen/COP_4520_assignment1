import java.util.Arrays;

public class A1Sync {
	private static boolean A[];
	private static long sumPrimes = 0;

	public static void main(String[] args) {
		// testing counter
		if(args.length == 0) {
			System.out.println("enter the number to stop at as an argument (eg: java A1 100)");
			System.exit(0);
		}
		final int MAX = Integer.parseInt(args[0]) + 1;
		assert(MAX > 2); // ensure the user enters a positive number

		// setup the array
		A = new boolean[MAX];
		Arrays.fill(A, true);
		A[0] = false;
		A[1] = false;

		// solve the problem
		long start = System.currentTimeMillis();
		int totalPrimes = countPrimes(MAX);
		long finish = System.currentTimeMillis();
		long elapsed = finish - start;
		printAnswer(elapsed, totalPrimes, sumPrimes);
	}
	// print in format professor is looking for
	private static void printAnswer(long elapsed, int totalPrimes, long sumPrimes){
		System.out.println(((double)elapsed/1000) + "s " + totalPrimes + " " + sumPrimes);
		int i = A.length-1;
		int count = 0;
		while(count < 10){
			count++;
			while(!A[i]){
				i--; // find the first prime
			}
			System.out.println(i);
			i--; // go down one.
		}
	}

	// not exactly sieve of eratosthenes but similar
	private static int countPrimes(int max){
		int sqrt = (int) Math.sqrt(max) + 1;

		// brute force all values under sqrt of n..
		for (int i = 2; i < sqrt; i++) {
			if(isPrime(i)){
				A[i] = true;
			} else {
				A[i] = false;
			}
		}

		// mark all values now..
		for (int i = 2; i < sqrt; i++) {
			if(A[i]){
				int squared = i*i;
				int j = 0;
				int next = squared + j*i;
				while (next < max){
					A[next] = false;
					j++;
					next = squared + j*i;
				}
			}
		}

		// count and sum all values now
		int count = 0;
		for (int i = 2; i < max; i++) {
			if(A[i]){
				count++;
				sumPrimes += i;
			}
		}


		return count;
	}

	private static boolean isPrime(int v){
		if(v <= 2) return true;

		int sqrt = (int) Math.sqrt(v);
		for (int i = 2; i <= sqrt; i++) {
			if(v % i == 0) return false;
		}
		return true;
	}
}
