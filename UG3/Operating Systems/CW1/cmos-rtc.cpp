/*
 * CMOS Real-time Clock
 * SKELETON IMPLEMENTATION -- TO BE FILLED IN FOR TASK (1)
 */

/*
 * STUDENT NUMBER: s1828233
 */
#include <infos/drivers/timer/rtc.h>
#include <infos/util/lock.h>
#include <arch/x86/pio.h>

using namespace infos::drivers;
using namespace infos::drivers::timer;
using namespace infos::util;
using namespace infos::arch::x86;

class CMOSRTC : public RTC {
public:
	static const DeviceClass CMOSRTCDeviceClass;
	int const CMOS_ADDRESS = 0x70;
	int const CMOS_DATA = 0x71;

	const DeviceClass& device_class() const override
	{
		return CMOSRTCDeviceClass;
	}

	/**
	 * Interrogates the RTC to read the current date & time.
	 * @param tp Populates the tp structure with the current data & time, as
	 * given by the CMOS RTC device.
	 */
	void read_timepoint(RTCTimePoint& current) override
	{
		// Follows method found on: https://wiki.osdev.org/CMOS#Reading_All_RTC_Time_and_Date_Registers
		// Reads timepoints until we get two values in a row.
		// Helps avoid getting dodgy/inconsistent values due to RTC updates.
		current = get_timepoint();
		RTCTimePoint last = current;

		do {
			last = current;
			current = get_timepoint();
		} while (!tp_equal(current, last));
		
		// If needed convert timepoint to binary
		if (in_bcd()) {
			get_tp_from_bcd(current);
		}
	
		// If in 12hr clock and the PM bit is set
		if (!(read_cmos_register_bit(0xB, 1)) && (current.hours & 0x80)) {
			// Convert to 24 hrs
			current.hours = ((current.hours & 0x7F) + 12) % 24;
		}
	}

	
	// Checks to see if two timepoints are equal
	bool tp_equal(RTCTimePoint t1, RTCTimePoint t2) {
		return (
			(t1.seconds == t2.seconds) &&
			(t1.minutes == t2.minutes) &&
			(t1.hours == t2.hours) &&
			(t1.day_of_month == t2.day_of_month) &&
			(t1.month == t2.month) &&
			(t1.year == t2.year) &&
			true
		);
	}


	// Converts a timepoint from BCD to binary values
	void get_tp_from_bcd(RTCTimePoint& tp)
	{
		tp.seconds = convert_from_bcd(tp.seconds);
		tp.minutes = convert_from_bcd(tp.minutes);
		tp.hours = convert_from_bcd(tp.hours);
		tp.day_of_month = convert_from_bcd(tp.day_of_month);
		tp.month = convert_from_bcd(tp.month);
		tp.year = convert_from_bcd(tp.year);
	}


	// Convert BCD to binary values
	unsigned short convert_from_bcd(unsigned short n) {
		return ((n / 16) * 10) + (n & 0xF);
	}


	// Reads the CMOS to check if in BCD mode
	bool in_bcd()
	{
		// Checks interrupts are disabled when accessing the RTC
		UniqueIRQLock l;

		// Read bit 2 from status register B
		uint8_t bit = read_cmos_register_bit(0xB, 2);

		// bit is zero if register values are in BCD
		return bit == 0;
	}


	// Reads a register from the CMOS
	uint8_t read_cmos_register(int reg)
	{
		__outb(CMOS_ADDRESS, reg);
		return __inb(CMOS_DATA);
	}


	// Reads a specified bit from a register from the CMOS
	uint8_t read_cmos_register_bit(int reg, int bit)
	{
		return (read_cmos_register(reg) >> bit) & 1;
	}

	
	// Reads the CMOS to check if an RTC update is in progress
	bool update_in_progress()
	{
		// Read bit 7 from status register A
		auto bit = read_cmos_register_bit(0xA, 7);

		// Bit is not zero if an update is in progress
		return bit != 0;
	}


	//Reads a timepoint from the RTC.
	RTCTimePoint get_timepoint()
	{
		// Checks interrupts are disabled when accessing the RTC
		UniqueIRQLock l;

		// Wait for current update to finish before reading timepoints
		while (update_in_progress()) {
			// Waiting...
		}

		// Return timepoint from cmos once RTC update finished
		return RTCTimePoint{
			.seconds = read_cmos_register(0x00),
			.minutes = read_cmos_register(0x02),
			.hours = read_cmos_register(0x04),
			.day_of_month = read_cmos_register(0x07),
			.month = read_cmos_register(0x08),
			.year = read_cmos_register(0x09),
		};
	}

};

const DeviceClass CMOSRTC::CMOSRTCDeviceClass(RTC::RTCDeviceClass, "cmos-rtc");

RegisterDevice(CMOSRTC);
