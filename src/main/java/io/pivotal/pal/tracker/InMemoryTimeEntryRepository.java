package io.pivotal.pal.tracker;

import java.util.ArrayList;
import java.util.List;

public class InMemoryTimeEntryRepository implements TimeEntryRepository {

    List<TimeEntry> timeEntries = new ArrayList<>();
    long entryIds = 0;

    @Override
    public TimeEntry create(TimeEntry timeEntry) {
        timeEntry.setId(++entryIds);
        timeEntries.add(timeEntry);
        return timeEntry;
    }

    @Override
    public TimeEntry find(long timeEntryId) {

        return timeEntries.stream()
                .filter(t -> t.getId() == timeEntryId)
                .findFirst().orElse(null);
    }

    @Override
    public List list() {
        return timeEntries;
    }

    @Override
    public TimeEntry update(long eq, TimeEntry any) {
        if(find(eq) == null) {
            return null;
        }
        delete(eq);
        any.setId(eq);
        list().add(any);
        return any;
    }

    @Override
    public void delete(long timeEntryId) {
        timeEntries.remove(find(timeEntryId));

    }
}