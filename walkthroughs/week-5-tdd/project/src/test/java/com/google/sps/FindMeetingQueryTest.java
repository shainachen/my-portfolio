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

import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** */
@RunWith(JUnit4.class)
public final class FindMeetingQueryTest {
  private static final Set<Event> NO_EVENTS = Collections.emptySet();
  private static final Set<String> NO_ATTENDEES = Collections.emptySet();

  // Some people that we can use in our tests.
  private static final String PERSON_A = "Person A";
  private static final String PERSON_B = "Person B";
  private static final String PERSON_C = "Person C";

  // All dates are the first day of the year 2020.
  private static final int TIME_0800AM = TimeRange.getTimeInMinutes(8, 0);
  private static final int TIME_0830AM = TimeRange.getTimeInMinutes(8, 30);
  private static final int TIME_0900AM = TimeRange.getTimeInMinutes(9, 0);
  private static final int TIME_0930AM = TimeRange.getTimeInMinutes(9, 30);
  private static final int TIME_1000AM = TimeRange.getTimeInMinutes(10, 0);
  private static final int TIME_1100AM = TimeRange.getTimeInMinutes(11, 00);

  private static final int DURATION_15_MINUTES = 15;
  private static final int DURATION_30_MINUTES = 30;
  private static final int DURATION_60_MINUTES = 60;
  private static final int DURATION_90_MINUTES = 90;
  private static final int DURATION_1_HOUR = 60;
  private static final int DURATION_2_HOUR = 120;

  private FindMeetingQuery query;

  @Before
  public void setUp() {
    query = new FindMeetingQuery();
  }

  @Test
  public void optionsForNoAttendees() {
    MeetingRequest request = new MeetingRequest(NO_ATTENDEES, DURATION_1_HOUR);

    Collection<TimeRange> actual = query.query(NO_EVENTS, request);
    Collection<TimeRange> expected = Arrays.asList(TimeRange.WHOLE_DAY);

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void noOptionsForTooLongOfARequest() {
    // The duration should be longer than a day. This means there should be no options.
    int duration = TimeRange.WHOLE_DAY.duration() + 1;
    MeetingRequest request = new MeetingRequest(ImmutableSet.of(PERSON_A), duration);

    Collection<TimeRange> actual = query.query(NO_EVENTS, request);
    Collection<TimeRange> expected = Arrays.asList();

    Assert.assertEquals(expected, actual);
  }
  
  @Test
  public void eventSplitsRestriction() {
    // The event should split the day into two options (before and after the event).
    Collection<Event> events = Arrays.asList(new Event("Event 1",
        TimeRange.fromStartDuration(TIME_0830AM, DURATION_30_MINUTES), ImmutableSet.of(PERSON_A)));

    MeetingRequest request = new MeetingRequest(ImmutableSet.of(PERSON_A), DURATION_30_MINUTES);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected =
        Arrays.asList(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0830AM, false),
            TimeRange.fromStartEnd(TIME_0900AM, TimeRange.END_OF_DAY, true));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void everyAttendeeIsConsidered() {
    // Have each person have different events. We should see two options because each person has
    // split the restricted times.
    //
    // Events  :       |--A--|     |--B--|
    // Day     : |-----------------------------|
    // Options : |--1--|     |--2--|     |--3--|

    Collection<Event> events = Arrays.asList(
        new Event("Event 1", TimeRange.fromStartDuration(TIME_0800AM, DURATION_30_MINUTES),
            ImmutableSet.of(PERSON_A)),
        new Event("Event 2", TimeRange.fromStartDuration(TIME_0900AM, DURATION_30_MINUTES),
            ImmutableSet.of(PERSON_B)));

    MeetingRequest request =
        new MeetingRequest(ImmutableSet.of(PERSON_A, PERSON_B), DURATION_30_MINUTES);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected =
        Arrays.asList(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0800AM, false),
            TimeRange.fromStartEnd(TIME_0830AM, TIME_0900AM, false),
            TimeRange.fromStartEnd(TIME_0930AM, TimeRange.END_OF_DAY, true));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void overlappingEvents() {
    // Have an event for each person, but have their events overlap. We should only see two options.
    //
    // Events  :       |--A--|
    //                     |--B--|
    // Day     : |---------------------|
    // Options : |--1--|         |--2--|

    Collection<Event> events = Arrays.asList(
        new Event("Event 1", TimeRange.fromStartDuration(TIME_0830AM, DURATION_60_MINUTES),
            ImmutableSet.of(PERSON_A)),
        new Event("Event 2", TimeRange.fromStartDuration(TIME_0900AM, DURATION_60_MINUTES),
            ImmutableSet.of(PERSON_B)));

    MeetingRequest request =
        new MeetingRequest(ImmutableSet.of(PERSON_A, PERSON_B), DURATION_30_MINUTES);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected =
        Arrays.asList(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0830AM, false),
            TimeRange.fromStartEnd(TIME_1000AM, TimeRange.END_OF_DAY, true));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void nestedEvents() {
    // Have an event for each person, but have one person's event fully contain another's event. We
    // should see two options.
    //
    // Events  :       |----A----|
    //                   |--B--|
    // Day     : |---------------------|
    // Options : |--1--|         |--2--|

    Collection<Event> events = Arrays.asList(
        new Event("Event 1", TimeRange.fromStartDuration(TIME_0830AM, DURATION_90_MINUTES),
            ImmutableSet.of(PERSON_A)),
        new Event("Event 2", TimeRange.fromStartDuration(TIME_0900AM, DURATION_30_MINUTES),
            ImmutableSet.of(PERSON_B)));

    MeetingRequest request =
        new MeetingRequest(ImmutableSet.of(PERSON_A, PERSON_B), DURATION_30_MINUTES);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected =
        Arrays.asList(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0830AM, false),
            TimeRange.fromStartEnd(TIME_1000AM, TimeRange.END_OF_DAY, true));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void doubleBookedPeople() {
    // Have one person, but have them registered to attend two events at the same time.
    //
    // Events  :       |----A----|
    //                     |--A--|
    // Day     : |---------------------|
    // Options : |--1--|         |--2--|

    Collection<Event> events = Arrays.asList(
        new Event("Event 1", TimeRange.fromStartDuration(TIME_0830AM, DURATION_60_MINUTES),
            ImmutableSet.of(PERSON_A)),
        new Event("Event 2", TimeRange.fromStartDuration(TIME_0900AM, DURATION_30_MINUTES),
            ImmutableSet.of(PERSON_A)));

    MeetingRequest request = new MeetingRequest(ImmutableSet.of(PERSON_A), DURATION_30_MINUTES);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected =
        Arrays.asList(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0830AM, false),
            TimeRange.fromStartEnd(TIME_0930AM, TimeRange.END_OF_DAY, true));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void justEnoughRoom() {
    // Have one person, but make it so that there is just enough room at one point in the day to
    // have the meeting.
    //
    // Events  : |--A--|     |----A----|
    // Day     : |---------------------|
    // Options :       |-----|

    Collection<Event> events = Arrays.asList(
        new Event("Event 1", TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0830AM, false),
            ImmutableSet.of(PERSON_A)),
        new Event("Event 2", TimeRange.fromStartEnd(TIME_0900AM, TimeRange.END_OF_DAY, true),
            ImmutableSet.of(PERSON_A)));

    MeetingRequest request = new MeetingRequest(ImmutableSet.of(PERSON_A), DURATION_30_MINUTES);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected =
        Arrays.asList(TimeRange.fromStartDuration(TIME_0830AM, DURATION_30_MINUTES));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void ignoresPeopleNotAttending() {
    // Add an event, but make the only attendee someone different from the person looking to book
    // a meeting. This event should not affect the booking.
    Collection<Event> events = Arrays.asList(new Event("Event 1",
        TimeRange.fromStartDuration(TIME_0900AM, DURATION_30_MINUTES), ImmutableSet.of(PERSON_A)));
    MeetingRequest request = new MeetingRequest(ImmutableSet.of(PERSON_B), DURATION_30_MINUTES);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected = Arrays.asList(TimeRange.WHOLE_DAY);

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void noConflicts() {
    MeetingRequest request =
        new MeetingRequest(ImmutableSet.of(PERSON_A, PERSON_B), DURATION_30_MINUTES);

    Collection<TimeRange> actual = query.query(NO_EVENTS, request);
    Collection<TimeRange> expected = Arrays.asList(TimeRange.WHOLE_DAY);

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void notEnoughRoom() {
    // Have one person, but make it so that there is not enough room at any point in the day to
    // have the meeting.
    //
    // Events  : |--A-----| |-----A----|
    // Day     : |---------------------|
    // Options :

    Collection<Event> events = Arrays.asList(
        new Event("Event 1", TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0830AM, false),
            ImmutableSet.of(PERSON_A)),
        new Event("Event 2", TimeRange.fromStartEnd(TIME_0900AM, TimeRange.END_OF_DAY, true),
            ImmutableSet.of(PERSON_A)));

    MeetingRequest request = new MeetingRequest(ImmutableSet.of(PERSON_A), DURATION_60_MINUTES);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected = Arrays.asList();

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void testQuery_optionalAttendeeHasConflict_returnsValidTimesForOtherGuests() {
    // Optional attendee has an all day event. We should see three options because each person has
    // split the restricted times, and the optional attendee's events are not considered since no
    // time slot exists such that mandatory and optional attendees can attend.
    //
    // Events  :       |--A--|     |--B--|
    //           |---------C(optional)---------|
    // Day     : |-----------------------------|
    // Options : |--1--|     |--2--|     |--3--|
    
    Collection<Event> events = Arrays.asList(
        new Event("Event 1", TimeRange.fromStartDuration(TIME_0800AM, DURATION_30_MINUTES),
            ImmutableSet.of(PERSON_A)),
        new Event("Event 2", TimeRange.fromStartDuration(TIME_0900AM, DURATION_30_MINUTES),
            ImmutableSet.of(PERSON_B)),
        new Event("Event 3", TimeRange.fromStartDuration(TimeRange.START_OF_DAY, TimeRange.WHOLE_DAY.duration()),
            ImmutableSet.of(PERSON_C)));
    MeetingRequest request =
        new MeetingRequest(ImmutableSet.of(PERSON_A, PERSON_B), DURATION_30_MINUTES);
    request.addOptionalAttendee(PERSON_C);

    Collection<TimeRange> expected =
    Arrays.asList(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0800AM, false),
        TimeRange.fromStartEnd(TIME_0830AM, TIME_0900AM, false),
        TimeRange.fromStartEnd(TIME_0930AM, TimeRange.END_OF_DAY, true));
    Collection<TimeRange> actual = query.query(events, request);

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void testQuery_optionalAttendeeHasAvailability_returnsValidTimesForAllAttendees() {
    // Optional attendee is considered because there are time slots that exist
    // such that both mandatory and optional attendees can attend.
    //
    // Events  :       |--A--|     |--B--|
    //                       |--C--| (optional)
    // Day     : |-----------------------------|
    // Options : |--1--|                 |--2--|
    
    Collection<Event> events = Arrays.asList(
        new Event("Event 1", TimeRange.fromStartDuration(TIME_0800AM, DURATION_30_MINUTES),
            ImmutableSet.of(PERSON_A)),
        new Event("Event 2", TimeRange.fromStartDuration(TIME_0900AM, DURATION_30_MINUTES),
            ImmutableSet.of(PERSON_B)),
        new Event("Event 3", TimeRange.fromStartDuration(TIME_0830AM, DURATION_30_MINUTES),
            ImmutableSet.of(PERSON_C)));
    MeetingRequest request =
        new MeetingRequest(ImmutableSet.of(PERSON_A, PERSON_B), DURATION_30_MINUTES);
    request.addOptionalAttendee(PERSON_C);

    Collection<TimeRange> expected =
    Arrays.asList(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0800AM, false),
        TimeRange.fromStartEnd(TIME_0930AM, TimeRange.END_OF_DAY, true));
    Collection<TimeRange> actual = query.query(events, request);

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void testQuery_optionalAttendeeCannotSatisfyMeetingDuration_returnsValidTimesForOtherGuests() {
    // Optional attendee is ignored because considering their schedule results in a smaller time slot
    // than the requested meeting duration.
    //
    // Events  : |--A--|     |----A----|
    //                 |-C-| (optional)
    // Day     : |---------------------|
    // Options :       |-----|
    
    Collection<Event> events = Arrays.asList(
        new Event("Event 1", TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0830AM, false),
            ImmutableSet.of(PERSON_A)),
        new Event("Event 2", TimeRange.fromStartEnd(TIME_0900AM, TimeRange.END_OF_DAY, true),
            ImmutableSet.of(PERSON_A)),
        new Event("Event 3", TimeRange.fromStartDuration(TIME_0830AM, DURATION_15_MINUTES),
            ImmutableSet.of(PERSON_B)));
    MeetingRequest request = new MeetingRequest(ImmutableSet.of(PERSON_A), DURATION_30_MINUTES);
    request.addOptionalAttendee(PERSON_B);

    Collection<TimeRange> expected =
    Arrays.asList(TimeRange.fromStartDuration(TIME_0830AM, DURATION_30_MINUTES));
    Collection<TimeRange> actual = query.query(events, request);

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void testQuery_onlyOptionalAttendeesInMeeting_returnsValidTimes() {
    // Only optional attendees are in the meeting request.
    // Expected returned time ranges are responsive to all optional attendee conflicts.
    // Events  : |--A--|      |--A--|            (optional)
    //                 |-B-|             |--B--| (optional)        
    // Day     : |-----------------------------|
    // Options :           |--|     |----|
    Collection<Event> events = Arrays.asList(
        new Event("Event 1", TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0800AM, false),
            ImmutableSet.of(PERSON_A)),
        new Event("Event 2", TimeRange.fromStartEnd(TIME_0930AM, TIME_1000AM, false),
            ImmutableSet.of(PERSON_A)),
        new Event("Event 3", TimeRange.fromStartEnd(TIME_0800AM, TIME_0830AM, false),
            ImmutableSet.of(PERSON_B)),
        new Event("Event 4", TimeRange.fromStartEnd(TIME_1100AM, TimeRange.END_OF_DAY, true),
            ImmutableSet.of(PERSON_B)));
    MeetingRequest request = new MeetingRequest(ImmutableSet.of(), DURATION_30_MINUTES);
    request.addOptionalAttendee(PERSON_A);
    request.addOptionalAttendee(PERSON_B);

    Collection<TimeRange> expected =
    Arrays.asList(TimeRange.fromStartDuration(TIME_0830AM, DURATION_1_HOUR),
                  TimeRange.fromStartDuration(TIME_1000AM, DURATION_1_HOUR));
    Collection<TimeRange> actual = query.query(events, request);

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void testQuery_optionalAttendeesHaveNoAvailability_returnsNoTimes() {
    // Only optional attendees are in the meeting request.
    // Optional attendees do not have gaps in schedules, so no time ranges are returned.
    // Events  : |--A--|     |--A--|       (optional)
    //                 |--B--|     |--B--| (optional)        
    // Day     : |-----------------------|
    // Options :
    Collection<Event> events = Arrays.asList(
        new Event("Event 1", TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0800AM, false),
            ImmutableSet.of(PERSON_A)),
        new Event("Event 2", TimeRange.fromStartEnd(TIME_1000AM, TIME_1100AM, false),
            ImmutableSet.of(PERSON_A)),
        new Event("Event 3", TimeRange.fromStartEnd(TIME_0800AM, TIME_1000AM, false),
            ImmutableSet.of(PERSON_B)),
        new Event("Event 4", TimeRange.fromStartEnd(TIME_1100AM, TimeRange.END_OF_DAY, true),
            ImmutableSet.of(PERSON_B)));
    MeetingRequest request = new MeetingRequest(ImmutableSet.of(), DURATION_30_MINUTES);
    request.addOptionalAttendee(PERSON_A);
    request.addOptionalAttendee(PERSON_B);

    Collection<TimeRange> expected = Arrays.asList();
    Collection<TimeRange> actual = query.query(events, request);
    
    Assert.assertEquals(expected, actual);
  }
}
