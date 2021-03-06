package org.talend.dataquality.matchmerge;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

public class AttributeValues<T> implements Iterable<T> {

    private final TreeSet<Entry<T>> values = new TreeSet<Entry<T>>();

    private int size = 0;

    public synchronized Entry<T> get(T value) {
        for (Entry<T> tEntry : values) {
            if (ObjectUtils.equals(tEntry.value, value)) {
                return tEntry;
            }
        }
        Entry<T> newEntry = new Entry<T>(value);
        values.add(newEntry);
        return newEntry;
    }

    public void merge(AttributeValues<T> other) {
        for (Entry<T> value : other.values) {
            get(value.value).add(value.occurrence);
        }
    }

    public T mostCommon() {
        if (values.isEmpty()) {
            return null;
        }
        return values.last().value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AttributeValues)) {
            return false;
        }
        AttributeValues<T> that = (AttributeValues<T>) o;
        if (!values.equals(that.values)) {
            for (Entry<T> value : values) {
                Entry entry = that.get(value.value);
                if (entry.occurrence != value.occurrence) {
                    return false;
                }
            }
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }

    public Iterator<T> iterator() {
        final Iterator<Entry<T>> iterator = values.iterator();
        return new Iterator<T>() {

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public T next() {
                return iterator.next().value;
            }

            @Override
            public void remove() {
                iterator.remove();
            }
        };
    }

    public List<T> asList() {
        List<T> list = new ArrayList<T>(size);
        for (Entry<T> value : values) {
            for (int i = 0; i < value.occurrence; i++) {
                list.add(value.value);
            }
        }
        return list;
    }

    public boolean hasMultipleValues() {
        Iterator<Entry<T>> iterator = values.iterator();
        boolean asOneElement = iterator.hasNext();
        boolean asMoreElements = false;
        if (iterator.hasNext()) {
            asMoreElements = iterator.hasNext();
        }
        return asOneElement && asMoreElements;
    }

    public int size() {
        return size;
    }

    public class Entry<T> implements Comparable<Entry<T>> {

        private final T value;

        private int occurrence = 0;

        public Entry(T value) {
            this.value = value;
        }

        public void add(int occurrence) {
            updateOccurrence(occurrence);
        }

        private void updateOccurrence(int occurrence) {
            this.occurrence += occurrence;
            size += occurrence;
        }

        public void increment() {
            updateOccurrence(1);
        }

        @Override
        public int compareTo(Entry<T> tEntry) {
            if (ObjectUtils.equals(value, tEntry.value)) {
                return 0;
            }
            return occurrence - tEntry.occurrence;
        }

        public int getOccurrence() {
            return occurrence;
        }
    }

}
