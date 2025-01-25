/**
 * Represents a managed memory space. The memory space manages a list of allocated 
 * memory blocks, and a list free memory blocks. The methods "malloc" and "free" are 
 * used, respectively, for creating new blocks and recycling existing blocks.
 */
public class MemorySpace {
	
	// A list of the memory blocks that are presently allocated
	private LinkedList allocatedList;

	// A list of memory blocks that are presently free
	private LinkedList freeList;

	/**
	 * Constructs a new managed memory space of a given maximal size.
	 * 
	 * @param maxSize
	 *            the size of the memory space to be managed
	 */
	public MemorySpace(int maxSize) {
		// initiallizes an empty list of allocated blocks.
		allocatedList = new LinkedList();
	    // Initializes a free list containing a single block which represents
	    // the entire memory. The base address of this single initial block is
	    // zero, and its length is the given memory size.
		freeList = new LinkedList();
		freeList.addLast(new MemoryBlock(0, maxSize));
	}

	/**
	 * Allocates a memory block of a requested length (in words). Returns the
	 * base address of the allocated block, or -1 if unable to allocate.
	 * 
	 * This implementation scans the freeList, looking for the first free memory block 
	 * whose length equals at least the given length. If such a block is found, the method 
	 * performs the following operations:
	 * 
	 * (1) A new memory block is constructed. The base address of the new block is set to
	 * the base address of the found free block. The length of the new block is set to the value 
	 * of the method's length parameter.
	 * 
	 * (2) The new memory block is appended to the end of the allocatedList.
	 * 
	 * (3) The base address and the length of the found free block are updated, to reflect the allocation.
	 * For example, suppose that the requested block length is 17, and suppose that the base
	 * address and length of the the found free block are 250 and 20, respectively.
	 * In such a case, the base address and length of of the allocated block
	 * are set to 250 and 17, respectively, and the base address and length
	 * of the found free block are set to 267 and 3, respectively.
	 * 
	 * (4) The new memory block is returned.
	 * 
	 * If the length of the found block is exactly the same as the requested length, 
	 * then the found block is removed from the freeList and appended to the allocatedList.
	 * 
	 * @param length
	 *        the length (in words) of the memory block that has to be allocated
	 * @return the base address of the allocated block, or -1 if unable to allocate
	 */
	public int malloc(int length) {
		Node current = freeList.iterator().current;
		Node previous = null;
	
		while (current != null) {
			MemoryBlock block = current.block;
	
			if (block.length >= length) { // Block can satisfy the request
				// Step 1: Create a new allocated memory block
				MemoryBlock allocatedBlock = new MemoryBlock(block.baseAddress, length);
	
				// Step 2: Add the allocated block to the allocatedList
				allocatedList.addLast(allocatedBlock);
	
				// Step 3: Adjust or remove the free block
				if (block.length == length) { // Exact fit
					if (previous == null) { // Removing the first node in freeList
						freeList.iterator().current = current.next;
					} else { // Removing a middle or last node
						previous.next = current.next;
					}
				} else { // Partial allocation
					block.baseAddress += length; // Adjust the base address of the free block
					block.length -= length; // Reduce the length of the free block
				}
	
				// Step 4: Return the base address of the allocated block
				return allocatedBlock.baseAddress;
			}
	
			previous = current;
			current = current.next; // Move to the next free block
		}
	
		// If no suitable block is found, return -1
		return -1;
	}
	/**
	 * Frees the memory block whose base address equals the given address.
	 * This implementation deletes the block whose base address equals the given 
	 * address from the allocatedList, and adds it at the end of the free list. 
	 * 
	 * @param baseAddress
	 *            the starting address of the block to freeList
	 */
	public void free(int address) {
		Node current = allocatedList.iterator().current;
		Node previous = null;
	
		while (current != null) {
			MemoryBlock block = current.block;
	
			if (block.baseAddress == address) { // Block found
				// Step 1: Remove the block from the allocatedList
				if (previous == null) { // Removing the first node
					allocatedList.iterator().current = current.next;
				} else {
					previous.next = current.next; // Bypass the current node
				}
	
				// Step 2: Add the block back to the freeList
				freeList.addLast(block);
	
				return; // Successfully freed
			}
	
			previous = current;
			current = current.next;
		}
	
		// If no matching block is found, throw an exception
		throw new IllegalArgumentException("Block with base address " + address + " not found in allocated list.");
	}
	
	/**
	 * A textual representation of the free list and the allocated list of this memory space, 
	 * for debugging purposes.
	 */
	public String toString() {
		return freeList.toString() + "\n" + allocatedList.toString();		
	}
	
	/**
	 * Performs defragmantation of this memory space.
	 * Normally, called by malloc, when it fails to find a memory block of the requested size.
	 * In this implementation Malloc does not call defrag.
	 */
	public void defrag() {
		if (freeList.iterator().current == null || freeList.iterator().current.next == null) {
			// Nothing to defragment if the freeList has 0 or 1 blocks
			return;
		}
	
		// Step 1: Sort the freeList by baseAddress
		boolean swapped;
		do {
			swapped = false;
			Node current = freeList.iterator().current;
			while (current != null && current.next != null) {
				if (current.block.baseAddress > current.next.block.baseAddress) {
					// Swap the two blocks
					MemoryBlock temp = current.block;
					current.block = current.next.block;
					current.next.block = temp;
					swapped = true;
				}
				current = current.next;
			}
		} while (swapped);
	
		// Step 2: Merge contiguous blocks
		Node current = freeList.iterator().current;
		while (current != null && current.next != null) {
			MemoryBlock currentBlock = current.block;
			MemoryBlock nextBlock = current.next.block;
	
			if (currentBlock.baseAddress + currentBlock.length == nextBlock.baseAddress) {
				// Merge the two blocks
				currentBlock.length += nextBlock.length; // Extend the current block
				current.next = current.next.next;       // Remove the next block
			} else {
				current = current.next; // Move to the next block
			}
		}
	}
	}
	

