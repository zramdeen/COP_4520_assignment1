import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

public class A1 {
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

		// for debug
//		System.out.println(Arrays.toString(A));


		/*
			to check
			100 				-> 25
			1,000 			-> 168
			10,000			-> 1,229
			1,000,000 	-> 78,498
			10,000,000	-> 664,579
			100,000,000	-> 5,761,455
		*/
	}

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

	private static int countPrimes(int max){
		Counter c = new Counter(2);

		// create 8 threads
		int totalThreads = 8;
		Thread tarr[] = new Thread[totalThreads];
		for (int i = 0; i < totalThreads; i++) {
			tarr[i] = new Thread(new Worker(c, max), "t" + i);
		}

		// start threads
		for (Thread t : tarr) {
			t.start();
		}

		// force main thread to wait for child threads to finish
		try {
			for (Thread t : tarr) {
				t.join();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// mark off remaining primes
		int sqrtMax = (int) Math.sqrt(max);
		for (int i = 2; i < sqrtMax; i++) {
			// find a prime
			if(A[i]){
				// mark off all multiples (should start after sqrt max but w/e)
				int j = 0;
				int squared = i*i;
				int next = squared;
				while(next < max){
					A[next] = false;
					j++;
					next = squared + i*j;
				}
			}
		}

		// get total
		int count = 0;
		int index = 0;
		long sum = 0;
		for (boolean a:A) {
			if(a) {
				count++;
				sumPrimes += index;
			}
			index++;
		}

		// answer
//		System.out.println("total primes <= " + (max-1) + " is " + count);
		return count;
	}

	// works on finding primes
	static class Worker implements Runnable {
		private final Counter c;
		private final int MAX;
		private final int SQRT_MAX;

		public Worker(Counter count, int max){
			this.c = count;
			this.MAX = max;
			this.SQRT_MAX = (int) Math.sqrt(max);
		}

		@Override
		public void run() {
			int cur = c.getAndIncrement();
			while(cur <= SQRT_MAX){
				// check if the current value is prime
				if(isPrime(cur)){
					A[cur] = true;
				} else {
					A[cur] = false;
				}

				// get next work.
				cur = c.getAndIncrement();
			}
		}

		private boolean isPrime(int v){
			if(v <= 2) return true;

			int sqrt = (int) Math.sqrt(v);
			for (int i = 2; i <= sqrt; i++) {
				if(v % i == 0) return false;
			}
			return true;
		}
	}

	static class Counter {
		private AtomicInteger count;

		public Counter(int initial){
			count = new AtomicInteger(initial);
		}

		public int getAndIncrement(){
			return count.getAndIncrement();
		}

		public int getValue(){
			return count.get();
		}
	}
}
