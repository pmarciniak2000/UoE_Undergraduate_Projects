/*
 * Buddy Page Allocation Algorithm
 * SKELETON IMPLEMENTATION -- TO BE FILLED IN FOR TASK (3)
 */

/*
 * STUDENT NUMBER: s1828233
 */
#include <infos/mm/page-allocator.h>
#include <infos/mm/mm.h>
#include <infos/kernel/kernel.h>
#include <infos/kernel/log.h>
#include <infos/util/math.h>
#include <infos/util/printf.h>

using namespace infos::kernel;
using namespace infos::mm;
using namespace infos::util;

#define MAX_ORDER	17

/**
 * A buddy page allocation algorithm.
 */
class BuddyPageAllocator : public PageAllocatorAlgorithm
{
private:
	/**
	 * Returns the number of pages that comprise a 'block', in a given order.
	 * @param order The order to base the calculation off of.
	 * @return Returns the number of pages in a block, in the order.
	 */
	static inline constexpr uint64_t pages_per_block(int order)
	{
		/* The number of pages per block in a given order is simply 1, shifted left by the order number.
		 * For example, in order-2, there are (1 << 2) == 4 pages in each block.
		 */
		return (1 << order);
	}
	
	/**
	 * Returns TRUE if the supplied page descriptor is correctly aligned for the 
	 * given order.  Returns FALSE otherwise.
	 * @param pgd The page descriptor to test alignment for.
	 * @param order The order to use for calculations.
	 */
	static inline bool is_correct_alignment_for_order(const PageDescriptor *pgd, int order)
	{
		// Calculate the page-frame-number for the page descriptor, and return TRUE if
		// it divides evenly into the number pages in a block of the given order.
		return (sys.mm().pgalloc().pgd_to_pfn(pgd) % pages_per_block(order)) == 0;
	}
	
	/** Given a page descriptor, and an order, returns the buddy PGD.  The buddy could either be
	 * to the left or the right of PGD, in the given order.
	 * @param pgd The page descriptor to find the buddy for.
	 * @param order The order in which the page descriptor lives.
	 * @return Returns the buddy of the given page descriptor, in the given order.
	 */
	PageDescriptor *buddy_of(PageDescriptor *pgd, int order)
	{
		// (1) Make sure 'order' is within range
		if (order >= MAX_ORDER) {
			return NULL;
		}

		// (2) Check to make sure that PGD is correctly aligned in the order
		if (!is_correct_alignment_for_order(pgd, order)) {
			return NULL;
		}
				
		// (3) Calculate the page-frame-number of the buddy of this page.
		// * If the PFN is aligned to the next order, then the buddy is the next block in THIS order.
		// * If it's not aligned, then the buddy must be the previous block in THIS order.
		uint64_t buddy_pfn = is_correct_alignment_for_order(pgd, order + 1) ?
			sys.mm().pgalloc().pgd_to_pfn(pgd) + pages_per_block(order) : 
			sys.mm().pgalloc().pgd_to_pfn(pgd) - pages_per_block(order);
		
		// (4) Return the page descriptor associated with the buddy page-frame-number.
		return sys.mm().pgalloc().pfn_to_pgd(buddy_pfn);
	}
	
	/**
	 * Inserts a block into the free list of the given order.  The block is inserted in ascending order.
	 * @param pgd The page descriptor of the block to insert.
	 * @param order The order in which to insert the block.
	 * @return Returns the slot (i.e. a pointer to the pointer that points to the block) that the block
	 * was inserted into.
	 */
	PageDescriptor **insert_block(PageDescriptor *pgd, int order)
	{
		// Starting from the _free_area array, find the slot in which the page descriptor
		// should be inserted.
		PageDescriptor **slot = &_free_areas[order];
		
		// Iterate whilst there is a slot, and whilst the page descriptor pointer is numerically
		// greater than what the slot is pointing to.
		while (*slot && pgd > *slot) {
			slot = &(*slot)->next_free;
		}
		
		// Insert the page descriptor into the linked list.
		pgd->next_free = *slot;
		*slot = pgd;
		
		// Return the insert point (i.e. slot)
		return slot;
	}
	
	/**
	 * Removes a block from the free list of the given order.  The block MUST be present in the free-list, otherwise
	 * the system will panic.
	 * @param pgd The page descriptor of the block to remove.
	 * @param order The order in which to remove the block from.
	 */
	void remove_block(PageDescriptor *pgd, int order)
	{
		// Starting from the _free_area array, iterate until the block has been located in the linked-list.
		PageDescriptor **slot = &_free_areas[order];
		while (*slot && pgd != *slot) {
			slot = &(*slot)->next_free;
		}

		// Make sure the block actually exists.  Panic the system if it does not.
		assert(*slot == pgd);
		
		// Remove the block from the free list.
		*slot = pgd->next_free;
		pgd->next_free = NULL;
	}
	
	/**
	 * Given a pointer to a block of free memory in the order "source_order", this function will
	 * split the block in half, and insert it into the order below.
	 * @param block_pointer A pointer to a pointer containing the beginning of a block of free memory.
	 * @param source_order The order in which the block of free memory exists.  Naturally,
	 * the split will insert the two new blocks into the order below.
	 * @return Returns the left-hand-side of the new block.
	 */
	PageDescriptor *split_block(PageDescriptor **block_pointer, int source_order)
	{	
		// Make sure the block_pointer is correctly aligned.
		assert(is_correct_alignment_for_order(*block_pointer, source_order));
		
		// Cannot split blocks of zero or -ve order so return the original block
		if (source_order <= 0) {
			return *block_pointer;
		}

		// Split the current block into a left-hand-side and a right-hand-side.
		PageDescriptor *left = *block_pointer;
		PageDescriptor *right = left + pages_per_block(source_order - 1);

		//Remove the left block from the order and add left and right blocks to lower order
		remove_block(left, source_order);
		insert_block(left, source_order-1);
		insert_block(right, source_order-1);
		
		return *block_pointer;		
	}
	
	/**
	 * Takes a block in the given source order, and merges it (and it's buddy) into the next order.
	 * This function assumes both the source block and the buddy block are in the free list for the
	 * source order.  If they aren't this function will panic the system.
	 * @param block_pointer A pointer to a pointer containing a block in the pair to merge.
	 * @param source_order The order in which the pair of blocks live.
	 * @return Returns the new slot that points to the merged block.
	 */
	PageDescriptor **merge_block(PageDescriptor **block_pointer, int source_order)
	{
		// Make sure the area_pointer is correctly aligned.
		assert(is_correct_alignment_for_order(*block_pointer, source_order));

		// Cannot merge the block in the top order
		if (source_order == MAX_ORDER-1) {
			return block_pointer;
		}

		PageDescriptor *left = *block_pointer;
		PageDescriptor *right = buddy_of(left, source_order);

		// Remove the left and right hand blocks from the order
		remove_block(left, source_order);
		remove_block(right, source_order);

		// Check order of blocks in buddy pair
		if (left < right) {
			return insert_block(left, source_order + 1);
		}
		else {
			return insert_block(right, source_order + 1);
		}	
	}
	
public:
	/**
	 * Constructs a new instance of the Buddy Page Allocator.
	 */
	BuddyPageAllocator() {
		// Iterate over each free area, and clear it.
		for (unsigned int i = 0; i < ARRAY_SIZE(_free_areas); i++) {
			_free_areas[i] = NULL;
		}
	}
	
	/**
	 * Allocates 2^order number of contiguous pages
	 * @param order The power of two, of the number of contiguous pages to allocate.
	 * @return Returns a pointer to the first page descriptor for the newly allocated page range, or NULL if
	 * allocation failed.
	 */
	PageDescriptor *alloc_pages(int order) override
	{

		// Check order is valid
		assert(order >= 0);
		assert(order <= MAX_ORDER);

		// Find the highest order which has a block and is non empty
		int ord = order;
		while( _free_areas[ord] == NULL && ord < MAX_ORDER) {
			ord++;
		}
		
		PageDescriptor *block_pointer = _free_areas[ord];
		
		// Split blocks until order reached
		for(int i = ord; i > order; i--) {
			block_pointer = split_block(&block_pointer, i);
		}
		
		// Remove the block of pages
		remove_block(block_pointer, order);
		return block_pointer;	
	}
	
	/* *
	 * Frees 2^order contiguous pages.
	 * @param pgd A pointer to an array of page descriptors to be freed.
	 * @param order The power of two number of contiguous pages to free.
	 */
	void free_pages(PageDescriptor *pgd, int order) override
		{
			// Make sure that the incoming page descriptor is correctly aligned
			// for the order on which it is being freed, for example, it is
			// illegal to free page 1 in order-1.
			assert(is_correct_alignment_for_order(pgd, order));

			// Check order is valid
			assert(order >= 0);
			assert(order <= MAX_ORDER);

			// Insert the block into free memory and get its slot
			PageDescriptor **free = insert_block(pgd, order);

			bool allFreed = false;

			// Loop until all pages that can be freed are freed
			while (order < MAX_ORDER - 1 && allFreed == false) {
				bool currFree = false;

				// Find buddy of current order
				PageDescriptor* order_buddy = buddy_of(*free,order);
				
				// Check if the buddy is free
				if((*free)->next_free == order_buddy || order_buddy->next_free == *free) {
					currFree = true;
				}
				
				// If free then merge block with buddy and move onto next order
				if(currFree) {
					free = merge_block(free, order);
					order++;
				}
				else {  
					allFreed = true;
				}
			}
		}
	
	/**
	 * Reserves a specific page, so that it cannot be allocated.
	 * @param pgd The page descriptor of the page to reserve.
	 * @return Returns TRUE if the reservation was successful, FALSE otherwise.
	 */
	bool reserve_page(PageDescriptor *pgd)
	{

		assert(pgd);

		bool found = false;

		// Loop through all orders
		for (int x = 0; x < MAX_ORDER; x++) {
			PageDescriptor *block_pointer = _free_areas[x];

			// Loop until we reach an empty free memory or find the page
			while(block_pointer != NULL && found == false) {
				PageDescriptor *pg_block;

			// If page descriptor here
				if (block_pointer <= pgd && pgd < (block_pointer + pages_per_block(x))) {
					pg_block = block_pointer;

					//Keep splitting until we get to the page
					for (int i = x; i > 0;) {
						pg_block = split_block(&pg_block, i);
						i--;

						// Check page not in right side buddy
						if (pgd >= pg_block + pages_per_block(i)) {
								pg_block = buddy_of(pg_block, i);
							}
					}
					// Page has been found so set found to true to end the loop 
					found = true;
				}
				else {
					// Move to the next block
					block_pointer = block_pointer->next_free;
				}
			}
		}
		// If page has been found, reserve it by removing it
		if (found == true) {
			remove_block(pgd, 0);
			return true;
		}
		else {
			return false;
		}	
	}
	
	/**
	 * Initialises the allocation algorithm.
	 * @return Returns TRUE if the algorithm was successfully initialised, FALSE otherwise.
	 */
	bool init(PageDescriptor *page_descriptors, uint64_t nr_page_descriptors) override
	{
		mm_log.messagef(LogLevel::DEBUG, "Buddy Allocator Initialising pd=%p, nr=0x%lx", page_descriptors, nr_page_descriptors);
		
		// Initialise the free area linked list for the maximum order
		// Initialise the allocation algorithm.

		// blocksize in the last order
		int block_size = pages_per_block(MAX_ORDER - 1);

		// Number of blocks in the last order
		int blocks = nr_page_descriptors/block_size;

		// Assign the first list of free areas for order-16
		_free_areas[MAX_ORDER - 1] = page_descriptors;
		
		// Fill the list		
		int i = 0;
		while(i < blocks) {
	
			// Join descriptor to the next block in the order
			page_descriptors->next_free = page_descriptors + block_size;
						
			// Move to next pointer
			page_descriptors = page_descriptors->next_free; 
	
			i++;
		}

		return true;
	}

	/**
	 * Returns the friendly name of the allocation algorithm, for debugging and selection purposes.
	 */
	const char* name() const override { return "buddy"; }
	
	/**
	 * Dumps out the current state of the buddy system
	 */
	void dump_state() const override
	{
		// Print out a header, so we can find the output in the logs.
		mm_log.messagef(LogLevel::DEBUG, "BUDDY STATE:");
		
		// Iterate over each free area.
		for (unsigned int i = 0; i < ARRAY_SIZE(_free_areas); i++) {
			char buffer[256];
			snprintf(buffer, sizeof(buffer), "[%d] ", i);
						
			// Iterate over each block in the free area.
			PageDescriptor *pg = _free_areas[i];
			while (pg) {
				// Append the PFN of the free block to the output buffer.
				snprintf(buffer, sizeof(buffer), "%s%lx ", buffer, sys.mm().pgalloc().pgd_to_pfn(pg));
				pg = pg->next_free;
			}
			
			mm_log.messagef(LogLevel::DEBUG, "%s", buffer);
		}
	}

	
private:
	PageDescriptor *_free_areas[MAX_ORDER];
};

/* --- DO NOT CHANGE ANYTHING BELOW THIS LINE --- */

/*
 * Allocation algorithm registration framework
 */
RegisterPageAllocator(BuddyPageAllocator);
