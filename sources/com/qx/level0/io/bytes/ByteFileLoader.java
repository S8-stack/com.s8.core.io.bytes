package com.qx.level0.io.bytes;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.READ;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;

import com.s8.api.io.ByteInflow;

/**
 * 
 * @author pc
 *
 */
public abstract class ByteFileLoader {
	
	private Path path;
	
	public ByteFileLoader(Path path) {
		super();
		this.path = path;
	}
	
	public Path getPath() {
		return path;
	}
	
	public void load() {
		load(ByteFile.DEFAULT_BUFFER_CAPACITY);
	}

	/**
	 * Test if file is existing
	 * 
	 * @return a flag indicating if loading is successful
	 * @throws BkException
	 * @throws IOException 
	 */
	public void load(int capacity) {
		if(Files.exists(path)) {

			try {
				FileChannel channel = FileChannel.open(path, CREATE, READ);
				FileByteInflow inflow = new FileByteInflow(channel, capacity);
				inflow.pull();

				consume(inflow);
				channel.close();
			}
			catch (IOException e) {
				onIOException(e);
			}
		}
		else {
			onFileDoesNotExist();
		}
	}
	
	
	/**
	 * 
	 * @param inflow
	 * @return true if inflow has been successfully consumed
	 * @throws IOException 
	 */
	public abstract void consume(ByteInflow inflow) throws IOException;


	public abstract void onFileDoesNotExist();
	
	public abstract void onIOException(IOException exception);
	
}
