import java.util.LinkedList;

public class CPU {
	static final int QUANTUM = 100;
	static final int MAX_MEM_TIME = 1000;
	static int currentQuantum;
	static int currentJobInd;
	LinkedList<Integer> readyQueue;
	
	public CPU(){
		currentJobInd = -1;
		readyQueue = new LinkedList<Integer>();
		currentQuantum = QUANTUM;
	}
	
	/**
	 *  a[0]: gives the state of the CPU:
  
       a[1]: no jobs to run, CPU will wait until interrupt (extremely rare situation, but it can come up!)
                 Ignore p values.
  
       a[2]: set CPU to run mode, must set p values as below.
  
           - p[0], p[1], and p[5]: ignored
           - p[2]: the base address of job to be run.
           - p[3]: the size (in K) of job to be run.
           - p[4]: time slice (time quantum)
	 * @param a
	 * @param p
	 */
	public int [] scheduler(int [] a, int [] p){
		//exceedTime[0] = exceeded maxCPUtime/quantumtime, free the memory by terminating
		//exceedTime[1] = exceeded maximum time in memory (1000)
		System.out.println("inside CPU, scheduler");
		int [] cpuMemExceed = {-1,-1};
		
		if(currentJobInd != -1){
			//time left in quantum
			if(currentQuantum > 0){
				//job have less than quantum time, run to finish
				//if not run quantum amount of time, switch to other jobs
				int timeLeft = JobTable.getTimeLeft(currentJobInd);
				currentQuantum = (timeLeft < QUANTUM)? timeLeft : QUANTUM;

				// Running stays the same
				System.out.println("-CPUScheduler resumes Job " + currentJobInd
				 + " with " + currentQuantum + " remaining");

			}
			// exceeded max CPU time， free it's memory and terminate it
			else if(JobTable.getTimeLeft(currentJobInd) <= 0){
				System.out.println("-CPUScheduler stops Job " + currentJobInd 
						+ " (exceeds max CPU time)");
				cpuMemExceed[0] = currentJobInd;
				JobTable.setTerminated(currentJobInd, true);
				JobTable.setReady(currentJobInd, false);
				currentJobInd = -1;
			}
			//exceed max memory time
			else if((os.currentTime - JobTable.getEnterTime(currentJobInd))
					>= MAX_MEM_TIME){
				if(!JobTable.isDoingIO(currentJobInd) && readyQueue.size() > 4){
					cpuMemExceed[1] = currentJobInd;
					JobTable.setReady(currentJobInd, false);
				}
				else{
					readyQueue.add(currentJobInd);
				}
				currentJobInd = -1;
			}
			//current time quantum used up, back to ready queue
			else{
				readyQueue.add(currentJobInd);
				currentJobInd = -1;
			}
		}//job running
		
		// no job running
		if(currentJobInd == -1 ){
			if(!readyQueue.isEmpty()){ //job waiting in readyQueue
				currentJobInd = readyQueue.poll();
				System.out.println("Next job: " + currentJobInd);
				
				int timeLeft = JobTable.getTimeLeft(currentJobInd);
				//job have less than quantum time, run to finish
				//if not run quantum amount of time, switch to other jobs
				currentQuantum = (timeLeft < QUANTUM)? timeLeft : QUANTUM;
				System.out.println("TIME LEFT: " + timeLeft);
			}
		
		}
		
		System.out.println("CURRENT QUANTUM: " + currentQuantum);
		
		if(currentJobInd == -1){ //still -1
			System.out.println("a set to 1..  no job to run");
			//no job running and waiting
			a[0] = 1;
		}
		else{
			System.out.println("a set to 2.. run mode");
			a[0] = 2;
			p[1] = currentJobInd;
			p[2] = JobTable.getAddress(currentJobInd);
			p[3] = JobTable.getSize(currentJobInd);
			p[4] = currentQuantum;
		}
		
		return cpuMemExceed;
	}// end of scheduler
	
	//readyQueue for cpu process later
	public void readyToRun(int jobNum){
		System.out.println("inside CPU, ready:");
		if(!JobTable.isTerminated(jobNum) && !JobTable.isBlocked(jobNum)
				&& !JobTable.isReady(jobNum)){
			System.out.println("adding to ready queue..");
			readyQueue.add(jobNum);
			JobTable.setReady(jobNum, true);
		}
			
	}
	
	//terminate current job running
	public int terminateJob(){
		System.out.println("inside CPU, terminateJob:");
		
		int jobTerminate = currentJobInd;
		currentJobInd = -1;
		
		JobTable.setTerminated(jobTerminate, true);
		JobTable.setReady(jobTerminate, false);
		System.out.println("terminate: " + jobTerminate);
		
		return jobTerminate;
	}
	
	//block current running job
	public void blockCurrentJob(){
		System.out.println("inside CPU, blockCurrentJob:");
		JobTable.setBlocked(currentJobInd, true);
		JobTable.setReady(currentJobInd, false);
		currentJobInd = -1;
	}
	
	//Keeps track of remaining Max CPU Time
	static void bookkeep(){
		System.out.println("Inside CPU, BOOKKEEP");
		int timeSpend = os.currentTime - os.timeBefore;
		System.out.println("TIME SPEND: " + timeSpend);
		if(currentJobInd != -1){
			JobTable.setTimeInCPU(currentJobInd, timeSpend);
			currentQuantum = currentQuantum - timeSpend;
		}
		System.out.println("CURRENT QUANTUM: " + currentQuantum);
	}
}
