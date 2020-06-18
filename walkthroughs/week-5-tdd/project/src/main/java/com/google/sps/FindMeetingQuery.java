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

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class FindMeetingQuery {
  /**
  * Returns time ranges that satisfy meeting request requirements and do not conflict with meeting
  * attendees' other events.
  * Considers mandatory and optional attendees. If no time ranges are available to accomodate both
  * types of attendees, returns time ranges that accomodate just mandatory attendees.
  * @param events collection of events happening during the day
  * @param request request for meeting with specified requirements (duration, attendees)
  * @return collection of time ranges that requested meeting can be held at. 
  */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    Collection<String> mandatoryAttendees = request.getAttendees();
    Collection<String> optionalAndMandatoryAttendees = Lists.newArrayList(Iterables.unmodifiableIterable(
      Iterables.concat(request.getOptionalAttendees(), mandatoryAttendees)));
    long meetingDuration = request.getDuration();
    
    if (meetingDuration > TimeRange.WHOLE_DAY.duration()) {
      return new ArrayList();
    }
    
    if (events.isEmpty()) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }

    Collection<TimeRange> availableTimes = availableTimeRanges(optionalAndMandatoryAttendees, events, meetingDuration);

    // If time slots exists that both mandatory and optional attendees can attend, return those.
    // Otherwise, return time slots that just fit mandatory attendees.
    return availableTimes.isEmpty() && !mandatoryAttendees.isEmpty() ? 
      availableTimeRanges(mandatoryAttendees, events, meetingDuration) : availableTimes; 
  }

  /**
  * Returns time ranges for meeting given list of meeting attendees. Helper method for query().
  * @param events collection of events happening during the day
  * @param meetingAttendees collection of meeting attendees
  * @param meetingDuration length of meeting in minutes
  * @return collection of time ranges that requested meeting can be held at 
  */
  private Collection<TimeRange> availableTimeRanges(Collection<String> meetingAttendees, Collection<Event> events, long meetingDuration) {
    Collection<TimeRange> availableTimes = new ArrayList();
    List<Event> eventsList = new ArrayList(events);

    //Order events by start time
    Collections.sort(eventsList, Event.ORDER_BY_START);
    int previousEndTime = TimeRange.START_OF_DAY;

    for (Event event : eventsList){
      //Check if meeting attendees are event attendees
      if (!Collections.disjoint(event.getAttendees(), meetingAttendees)) {
        int eventStart = event.getWhen().start();
        int eventEnd = event.getWhen().end();

        //Check if there is time to hold meeting before event starts
        if (previousEndTime + meetingDuration <= eventStart) {
          availableTimes.add(TimeRange.fromStartEnd(previousEndTime, eventStart, false));
        }

        //Considers nested event case, where event B is checked second and previousEndTime > event B's end.
        //In this case, we do not want to assign previousEndTime to be an earlier end time.
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
      availableTimes.add(TimeRange.fromStartEnd(previousEndTime, TimeRange.END_OF_DAY, true));
    }
    return availableTimes;
  }
}
