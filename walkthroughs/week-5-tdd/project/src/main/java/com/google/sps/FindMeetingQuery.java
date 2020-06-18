// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class FindMeetingQuery {
  /**
  * Returns time ranges that satisfy all meeting request requirements and do not conflict with meeting
  * attendees' other events for the day.
  * @param events collection of events happening during the day
  * @param request request for meeting with specified requirements (duration, attendees)
  * @return collection of time ranges that requested meeting can be held at 
  */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    Collection<String> meetingAttendees = request.getAttendees();
    Collection<String> optionalAttendees = request.getOptionalAttendees();
    long meetingDuration = request.getDuration();
    Collection<TimeRange> availableTimes = new ArrayList();
    Collection<TimeRange> availableTimesWithoutOptional = new ArrayList();

    if (meetingDuration > TimeRange.WHOLE_DAY.duration()) {
      return availableTimes;
    }
    
    if (events.isEmpty()) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }

    List<Event> eventsList = new ArrayList(events);
    Collections.sort(eventsList, Event.ORDER_BY_START);
    
    int previousEndTime = TimeRange.START_OF_DAY;
    for (Event event : eventsList){
      //Check if meeting attendees also are event attendees
      boolean mandatoryAttendeesAtEvent = !Collections.disjoint(event.getAttendees(), meetingAttendees);
      boolean optionalAttendeesAtEvent = !Collections.disjoint(event.getAttendees(), optionalAttendees);
      if (mandatoryAttendeesAtEvent || optionalAttendeesAtEvent) {
        int eventStart = event.getWhen().start();
        int eventEnd = event.getWhen().end();

        //Check if there is time to hold meeting before event starts
        if (previousEndTime + meetingDuration <= eventStart) {
          TimeRange openRange = TimeRange.fromStartEnd(previousEndTime, eventStart, false);
          if (mandatoryAttendeesAtEvent) {
            availableTimesWithoutOptional.add(openRange);
          }
          availableTimes.add(openRange);
        }

        //Considers nested event case, where event B is checked second and previousEndTime < event B's end
        //so we do not want to assign previousEndTime to be an earlier end time.
        // Events  :       |----A----|
        //                   |--B--|
        
        // Day     : |---------------------|
        // Options : |--1--|         |--2--|
        if (previousEndTime < eventEnd) {
          previousEndTime = eventEnd;
        }
      }
    }

    //Add remaining time of the day to available meeting time
    if (meetingDuration + previousEndTime <= TimeRange.END_OF_DAY) {
      TimeRange timeToEndOfDay = TimeRange.fromStartEnd(previousEndTime, TimeRange.END_OF_DAY, true);
      availableTimes.add(timeToEndOfDay);
      availableTimesWithoutOptional.add(timeToEndOfDay);
    }
    System.out.println("available times emtpy" + availableTimes.isEmpty());
    System.out.println("available without optional" + availableTimesWithoutOptional.size());
    return availableTimes.isEmpty() ? availableTimesWithoutOptional : availableTimes; 
  }

}
