/*
 * FIFO Scheduling Algorithm
 * SKELETON IMPLEMENTATION -- TO BE FILLED IN FOR TASK (2)
 */

/*
 * STUDENT NUMBER: s1828233
 */
#include <infos/kernel/sched.h>
#include <infos/kernel/thread.h>
#include <infos/kernel/log.h>
#include <infos/util/list.h>
#include <infos/util/lock.h>

using namespace infos::kernel;
using namespace infos::util;

/**
 * A FIFO scheduling algorithm
 */
class FIFOScheduler : public SchedulingAlgorithm
{
public:
	/**
	 * Returns the friendly name of the algorithm, for debugging and selection purposes.
	 */
	const char* name() const override { return "fifo"; }

	/**
	 * Called when a scheduling entity becomes eligible for running.
	 * @param entity
	 */
	void add_to_runqueue(SchedulingEntity& entity) override
	{
		// Check interrupts disabled when editing run queue
		UniqueIRQLock l;
		runqueue.enqueue(&entity);
	}

	/**
	 * Called when a scheduling entity is no longer eligible for running.
	 * @param entity
	 */
	void remove_from_runqueue(SchedulingEntity& entity) override
	{
		// Check interrupts disabled when editing run queue
		UniqueIRQLock l;
        	runqueue.remove(&entity); 
	}

	/**
	 * Called every time a scheduling event occurs, to cause the next eligible entity
	 * to be chosen.  The next eligible entity might actually be the same entity, if
	 * e.g. its timeslice has not expired, or the algorithm determines it's not time to change.
	 */
	SchedulingEntity *pick_next_entity() override
	{
		// If there's nothing in our queue, return nothing
		if (runqueue.count() == 0) {
			return NULL;
		}

		// If runqueue is not empty return the first element in the list
		return runqueue.first();
	}

	/** 
	 * Reason for sched-test2 running thread 1 infinitely:
	 * FIFO scheduling allows a thread to use the CPU as long as it needs, 
	 * so thread 1 uses the CPU, preventing thread 2 from using the CPU,
	 * so since thread 1 never stops executing it never starts thread 2
	 * so the program just runs thread 1 to infinity and doesnt't
	 * respond to any user input since all the CPU is used by thread 1
	 */

private:
	// A list containing the current runqueue.
	List<SchedulingEntity *> runqueue;
};

/* --- DO NOT CHANGE ANYTHING BELOW THIS LINE --- */

RegisterScheduler(FIFOScheduler);
