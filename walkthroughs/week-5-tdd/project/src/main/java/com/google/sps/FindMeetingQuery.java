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
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    Collection<String> meetingAttendees = request.getAttendees();
    long meetingDuration = request.getDuration();
    Collection<TimeRange> availableTimes = new ArrayList();

    if (meetingDuration > TimeRange.WHOLE_DAY.duration()) {
      return availableTimes;
    }
    
    if (events.isEmpty() || meetingAttendees.isEmpty()) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }

    List eventsList = new ArrayList(events);
    Collections.sort(eventsList, Event.ORDER_BY_START);
    
    int start = TimeRange.START_OF_DAY;
    for (Event event : events){
      if (!Collections.disjoint(event.getAttendees(), meetingAttendees)) {
        int eventStart = event.getWhen().start();
        int eventEnd = event.getWhen().end();

        if (start + meetingDuration <= eventStart) {
          availableTimes.add(TimeRange.fromStartEnd(start, eventStart, false));
        }

        if (eventEnd > start) {
          start = eventEnd;
        }
      }
    }

    if (meetingDuration + start <= TimeRange.END_OF_DAY) {
      availableTimes.add(TimeRange.fromStartEnd(start, TimeRange.END_OF_DAY, true));
    }
    return availableTimes;
  }
}
