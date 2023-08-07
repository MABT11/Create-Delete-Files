/**
* Create a file every 1 minutes and delete a file every 2 minutes
* */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;

public class SchedulerCreateDelete {
	public static class StopRun implements Runnable {
		BasicFileAttributes fileAttributes;
		File[] listOfFiles;
		BufferedWriter writer;
		FileWriter fileWriter;
		/** The thread delete will check and delete all files that are
		 *  last modified within the last 9 mintues
		 * */
		public static final int DELETEAFTER = 60 * 7;
		/**
		 * The thread create will create a new file every mintue
		 */
		public static final int CREATEAFTER = 60 * 1;
		/**
		 * The thread delete will run the delete logic every 6 mintue
		 */
		public static final int XAMOUNT = 60 * 6;
		/**
		 * A boolean value to terminate the threads 
		 */
		public boolean stop = false;
		/**
		 * loop control variable set to true to terminate the loop of creation and deletion 
		 */
		public synchronized void requestStop() {
			this.stop = true;
		}
		/**
		 * 
		 * @return the value of stop flag
		 */
		public synchronized boolean isStopRequested() {
			return this.stop;
		}
		/**
		 * customized sleep methods we pass the number in seconds and the method sleeps
		 * @param mili
		 */
		private void sleep(long mili) {
			mili *= 1000;
			try {
				Thread.sleep(mili);
			} catch (InterruptedException e) {
				System.out.println("Intrerrupted Exception Occured!");
			}
		}
		/**
		 * runs the logic of the threads based on their names
		 */
		@Override
		public void run() {
			String threadName = Thread.currentThread().getName();
			System.out.println("Running thread: " + threadName);
			String directoryPath = "NEEDS TO BE CHANGED";
			File directory = new File(directoryPath);
			if (!directory.exists()) {
				System.out.println("THE DIRECTORY REQUESTED IS NOT AVAILABLE");
				return;
			}
			// Each thread will run a different task based on the name of each thread
			if (threadName.compareTo("delete") == 0) {
				deleteScheduled(directory);
			} else if (threadName.compareTo("create") == 0) {
				createScheduled(directory);
			} else {
				return;
			}
			System.out.println("Thread " + threadName + " finished");
		}

		/**
		 * function that deletes the files if they have been last modified after being
		 * compared with DELETEAFTER constant
		 */
		public void deleteScheduled(File directory) {
			while (!isStopRequested()) {
				// you need it inside the while loop to make sure it get newly created files
				listOfFiles = directory.listFiles();

				// Loop through the files and call the helper function
				for (File files : listOfFiles) {
					loopDelete(files, directory);
				}
				listOfFiles = directory.listFiles();
				System.out.println("$ls");
				String l;
				for (File files : listOfFiles) {
					l = files.getPath();
					System.out.println(l.substring(l.lastIndexOf('\\')));
				}
				System.out.println("------------------------------");
				/**
				 * sleep for x amount periodically
				 */
				sleep(XAMOUNT);
			}
		}
		/**
		 * helper function for deleting files
		 * 
		 * @param files on each iteration this function will get called to either delete
		 *              the file or to keep it
		 * @return void
		 */
		public void loopDelete(File files, File directory) {
			try {
				// Access file attributes here
				fileAttributes = Files.readAttributes(files.toPath(), BasicFileAttributes.class);
				//calculating time to delete the files
				long lastModified = fileAttributes.lastModifiedTime().toMillis();
				long now = new Date().getTime();
				long duration = now - lastModified;
				duration = duration / (1000 * 60);
				if (duration > DELETEAFTER) {
//					System.out.print(files.getName());
//					System.out.println(" last modified time: " + duration);
				} else {
					System.out.println("deleting " + files.getName());
					files.delete();
				}
			} catch (IOException e) {
				System.out.println("IO EXCEPTION OCCURED when trying to delete file " + files.getName());
			}
		}
		/**
		 * Creates a new file periodically 
		 * @param directory passing the path to the directory 
		 * 
		 */
		public void createScheduled(File directory) {
			int i = 0;
			String fileName = "";

			while (!isStopRequested()) {
				fileName = directory + "/file" + i + ".txt";
				try {
					System.out.println("Creating a file: " + fileName.substring(directory.toString().length() + 1));
					fileWriter = new FileWriter(fileName);
					writer = new BufferedWriter(fileWriter);
					writer.write("" + (i + 1000));
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				sleep(CREATEAFTER);
				i++;
			}
			System.out.println("Created " + i + " files.");
		}
	}

	public static void main(String[] args) {
		
		/**
		 * creating the instance variables and the threads to run each logic
		 * doing this type of initialization just to make me remmber that i can create 
		 * and init in this fashion
		 */
		StopRun s2 = new StopRun(), s1 = new StopRun();
		Thread t2 = new Thread(s2, "delete"), t1 = new Thread(s1, "create");
		t1.start();
		t2.start();
		
		try {
			//run the program for 10 minutes by making the main thread sleep for 10 min
			Thread.currentThread().sleep(600_000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//stop both threads to terminate the program
		s1.requestStop();
		s2.requestStop();
	}
}
