package com.qx.level0.utilities.bytes;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * -Very- simple structure that prevent re-copying bytes array again and again for nothing.
 * Just traverse the ByteArrayChain with a simple:
 * <pre>{@code 
 * LinkedByteArray n;
 * while(n!=null){
 * // process here n.array
 * 	n = n.next;
 * }
 * </pre>
 * 
 * @author pc
 *
 */
public class QxBytes {


	/**
	 * the underlying bytes array of this link
	 */
	public byte[] bytes;


	/**
	 * Filled part bytes start at <code>offset</code>
	 */
	public int offset;

	/**
	 * Filled part of bytes is <code>length</code> long.
	 */
	public int length;

	public QxBytes next;

	public QxBytes(int capacity) {
		super();
		this.bytes = new byte[capacity];
		this.offset = 0;
		this.length = bytes.length;
	}

	public QxBytes(byte[] bytes) {
		super();
		this.bytes = bytes;
		this.offset = 0;
		this.length = bytes.length;
	}

	public QxBytes(byte[] bytes, int offset, int length) {
		super();
		this.bytes = bytes;
		this.offset = offset;
		this.length = length;
	}


	/**
	 * Useful utility function to flatten a chain-linked list of HTTP2_Fragment into
	 * a single ByteBuffer. Note that most of the time (99.99%), there is only one
	 * fragment so operation is trivial. That's why the reminaing part (0.01%) is
	 * not optimized.
	 * 
	 * @return a flattened ByteInflow from this fragment as a head
	 */
	public ByteBuffer flatten() {
		QxBytes fragment = this;
		if(fragment.next==null) {
			return ByteBuffer.wrap(fragment.bytes, fragment.offset, fragment.length);
		}
		else {
			int length=0;
			while(fragment!=null) {
				length+=fragment.length;
				fragment=fragment.next;
			}

			ByteBuffer buffer = ByteBuffer.allocate(length);
			fragment = this;
			while(fragment!=null) {
				buffer.put(fragment.bytes, fragment.offset, fragment.length);
				fragment=fragment.next;
			}
			return buffer;
		}
	}



	/**
	 * Flatten to byte Array
	 * 
	 * @return the byte array
	 */
	public byte[] toByteArray() {
		QxBytes fragment = this;

		int length=0;
		while(fragment!=null) {
			length+=fragment.length;
			fragment=fragment.next;
		}

		byte[] array = new byte[length];
		int index=0;
		fragment = this;
		while(fragment!=null) {
			byte[] fragmentBytes = fragment.bytes;
			int i0 = fragment.offset, i1= fragment.offset+fragment.length;
			for(int i=i0; i<i1; i++) {
				array[index++] = fragmentBytes[i];
			}
		}
		return array;
	}


	public QxBytes recut(int fragmentLength) {
		QxBytes chain1 = this;
		QxBytes chain2 = new QxBytes(new byte[fragmentLength], 0, 0);
		QxBytes head = chain2;

		int i2=0, i1 = chain1.offset;
		int n2 = fragmentLength, n1 = chain1.length;

		int nTransferredBytes;
		byte[] bytes2 = chain2.bytes, bytes1 = chain1.bytes;

		boolean isNextRequired2 = false;
		boolean isNextRequired1 = false;
		while(chain1!=null) {
			if(n2>n1) {
				isNextRequired2 = false;
				isNextRequired1 = true;
				nTransferredBytes = n1;
			}
			else if(n2<n1) {
				isNextRequired2 = true;
				isNextRequired1 = false;
				nTransferredBytes = n2;
			}
			else { // if(n0 == n1)
				isNextRequired2 = true;
				isNextRequired1 = true;
				nTransferredBytes = n2;
			}

			for(int i=0; i<nTransferredBytes; i++) {
				bytes2[i2++] = bytes1[i1++]; 
			}
			chain2.length+=nTransferredBytes;

			if(isNextRequired2) {
				chain2.next = new QxBytes(new byte[fragmentLength], 0, 0);
				chain2 = chain2.next;
				i2 = 0;
				n2 = fragmentLength;
				bytes2 = chain2.bytes;
			}
			else {
				n2-=nTransferredBytes;	
			}

			if(isNextRequired1) {
				chain1 = chain1.next;
				if(chain1!=null) {
					i1 = chain1.offset;
					n1 = chain1.length;
					bytes1 = chain1.bytes;	
				}
			}
			else {
				n1-=nTransferredBytes;
			}
		}
		
		return head;
	}
	
	
	/**
	 * retrieve tail of this chain
	 * @return
	 */
	public QxBytes tail() {
		QxBytes tail = this;
		while(tail.next!=null) {
			tail = tail.next;
		}
		return tail;
	}
	
	
	/**
	 * Append chain to this chain and return the tail chain link
	 * @param chain
	 * @return
	 */
	public QxBytes append(QxBytes chain) {
		QxBytes tail = tail();
		tail.next = chain;
		return tail.tail();
	}
	

	
	/**
	 * 
	 * @return
	 */
	public long getBytecount() {
		QxBytes link = this;
		long bytecount = 0;
		while(link!=null) {
			bytecount+=link.length;
			link = link.next;
		}
		return bytecount;
	}
	
	
	/**
	 * read this chain as an 
	 * @return
	 */
	public String toString_UTF8() {
		return new String(bytes, offset, length, StandardCharsets.UTF_8);
	}
	
	/**
	 * read this chain as an 
	 * @return
	 */
	public String unrollToString_UTF8() {
		StringBuilder builder = new StringBuilder();
		QxBytes chain = this;
		while(chain!=null) {
			builder.append(chain.toString_UTF8());
			chain = chain.next;
		}
		return builder.toString();
	}
	
	
	public static QxBytes fromString_UTF8(String str) {
		byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
		return new QxBytes(bytes);
	}
}
