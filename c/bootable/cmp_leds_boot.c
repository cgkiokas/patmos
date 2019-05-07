/*
    This is a bootable test program used as test
    for the lockstep project. Executes on all cores
    and switches three leds as well as write to the uart
    the state of the led.

    Author: Christos Gkiokas
*/

#include "include/bootable.h"



// main() is executed by all cores in parallel
int main() {
    volatile _IODEV int *uart_ptr = (volatile _IODEV int *) PATMOS_IO_VUARTCMP;
    volatile _IODEV int *led_ptr = (volatile _IODEV int *) PATMOS_IO_LEDSCMP;
    volatile _IODEV int *us_ptr = (volatile _IODEV int *) (PATMOS_IO_TIMER+12);


    int period = 1000;

    int time = period*1000/2;
    int next;

    for (;;) {

        next = *us_ptr + time;
        while (*us_ptr-next < 0){
            *led_ptr = 1;
            *uart_ptr = '1';
        }
        next = *us_ptr + time;
        while (*us_ptr-next < 0){
            *led_ptr = 0;
            *uart_ptr = '0';
        }
    }
    return 0;

}
