// Copyright (c) Corporation for National Research Initiatives
package org.python.core;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.python.util.Generic;

/**
 * Special fast dict implementation for __dict__ instances. Allows interned String keys in addition
 * to PyObject unlike PyDictionary.
 */
public class PyStringMap extends PyObject {

    private final ConcurrentMap<Object, PyObject> table;

    public PyStringMap() {
        this(4);
    }

    public PyStringMap(int capacity) {
        table = new ConcurrentHashMap<Object, PyObject>(capacity, Generic.CHM_LOAD_FACTOR,
                                                        Generic.CHM_CONCURRENCY_LEVEL);
    }

    public PyStringMap(Map<Object, PyObject> map) {
        table = Generic.concurrentMap();
        table.putAll(map);
    }

    public PyStringMap(PyObject elements[]) {
        this(elements.length);
        for (int i = 0; i < elements.length; i += 2) {
            __setitem__(elements[i], elements[i + 1]);
        }
    }

    public int __len__() {
        return table.size();
    }

    public boolean __nonzero__() {
        return table.size() != 0;
    }

    public PyObject __finditem__(String key) {
        if (key == null) {
            return null;
        }
        return table.get(key);
    }

    public PyObject __finditem__(PyObject key) {
        if (key instanceof PyString) {
            return __finditem__(((PyString)key).internedString());
        }
        return table.get(key);
    }

    public PyObject __getitem__(String key) {
        PyObject o = __finditem__(key);
        if (null == o) {
            throw Py.KeyError("'" + key + "'");
        } else {
            return o;
        }
    }

    public PyObject __getitem__(PyObject key) {
        if (key instanceof PyString) {
            return __getitem__(((PyString)key).internedString());
        } else {
            PyObject o = __finditem__(key);
            if (null == o) {
                throw Py.KeyError("'" + key.toString() + "'");
            } else {
                return o;
            }
        }
    }

    public PyObject __iter__() {
        return iterkeys();
    }

    public void __setitem__(String key, PyObject value) {
        if (value == null) {
            table.remove(key);
        } else {
            table.put(key, value);
        }
    }

    public void __setitem__(PyObject key, PyObject value) {
        if (value == null) {
            table.remove(pyToKey(key));
        } else if (key instanceof PyString) {
            __setitem__(((PyString)key).internedString(), value);
        } else {
            table.put(key, value);
        }
    }

    public void __delitem__(String key) {
        Object ret = table.remove(key);
        if (ret == null) {
            throw Py.KeyError(key);
        }
    }

    public void __delitem__(PyObject key) {
        if (key instanceof PyString) {
            __delitem__(((PyString)key).internedString());
        } else {
            Object ret = table.remove(key);
            if (ret == null) {
                throw Py.KeyError(key.toString());
            }
        }
    }

    /**
     * Remove all items from the dictionary.
     */
    public void clear() {
        table.clear();
    }

    public String toString() {
        ThreadState ts = Py.getThreadState();
        if (!ts.enterRepr(this)) {
            return "{...}";
        }
        StringBuilder buf = new StringBuilder("{");
        for (Entry<Object, PyObject> entry : table.entrySet()) {
            Object key = entry.getKey();
            if (key instanceof String) {
                // This is a bit complicated, but prevents us to duplicate
                // PyString#__repr__ logic here.
                buf.append(new PyString((String)key).__repr__().toString());
            } else {
                buf.append(((PyObject)key).__repr__().toString());
            }
            buf.append(": ");
            buf.append(entry.getValue().__repr__().toString());
            buf.append(", ");
        }
        if (buf.length() > 1) {
            buf.delete(buf.length() - 2, buf.length());
        }
        buf.append("}");
        ts.exitRepr(this);
        return buf.toString();
    }

    public int __cmp__(PyObject other) {
        if (!(other instanceof PyStringMap || other instanceof PyDictionary)) {
            return -2;
        }
        int an = __len__();
        int bn = other.__len__();
        if (an < bn) {
            return -1;
        }
        if (an > bn) {
            return 1;
        }
        PyList akeys = keys();
        PyList bkeys = null;
        if (other instanceof PyStringMap) {
            bkeys = ((PyStringMap)other).keys();
        } else {
            bkeys = ((PyDictionary)other).keys();
        }
        akeys.sort();
        bkeys.sort();
        for (int i = 0; i < bn; i++) {
            PyObject akey = akeys.pyget(i);
            PyObject bkey = bkeys.pyget(i);
            int c = akey._cmp(bkey);
            if (c != 0) {
                return c;
            }
            PyObject avalue = __finditem__(akey);
            PyObject bvalue = other.__finditem__(bkey);
            c = avalue._cmp(bvalue);
            if (c != 0) {
                return c;
            }
        }
        return 0;
    }

    /**
     * Return true if the key exist in the dictionary.
     */
    public boolean has_key(String key) {
        return table.containsKey(key);
    }

    public boolean has_key(PyObject key) {
        return table.containsKey(pyToKey(key));
    }

    /**
     * Return this[key] if the key exists in the mapping, default_object is returned otherwise.
     *
     * @param key
     *            the key to lookup in the mapping.
     * @param default_object
     *            the value to return if the key does not exists in the mapping.
     */
    public PyObject get(PyObject key, PyObject default_object) {
        PyObject o = __finditem__(key);
        if (o == null) {
            return default_object;
        } else {
            return o;
        }
    }

    /**
     * Return this[key] if the key exists in the mapping, None is returned otherwise.
     *
     * @param key
     *            the key to lookup in the mapping.
     */
    public PyObject get(PyObject key) {
        return get(key, Py.None);
    }

    /**
     * Return a shallow copy of the dictionary.
     */
    public PyStringMap copy() {
        return new PyStringMap(table);
    }

    /**
     * Insert all the key:value pairs from <code>dict</code> into this mapping.
     */
    public void update(PyObject[] args, String[] keywords) {
        int nargs = args.length - keywords.length;
        if (nargs > 1) {
            throw PyBuiltinCallable.DefaultInfo.unexpectedCall(nargs, false, "update", 0, 1);
        }
        if (nargs == 1) {
            PyObject arg = args[0];
            if (arg.__findattr__("keys") != null) {
                merge(arg);
            } else {
                mergeFromSeq(arg);
            }
        }
        for (int i = 0; i < keywords.length; i++) {
            __setitem__(keywords[i], args[nargs + i]);
        }
    }

    /**
     * Merge another PyObject that supports keys() with this
     * dict.
     *
     * @param other a PyObject with a keys() method
     */
    private void merge(PyObject other) {
        if (other instanceof PyStringMap) {
            table.putAll(((PyStringMap)other).table);
        } else if (other instanceof PyDictionary) {
            mergeFromKeys(other, ((PyDictionary)other).keys());
        } else {
            mergeFromKeys(other, other.invoke("keys"));
        }
    }

    /**
     * Merge another PyObject via its keys() method
     *
     * @param other a PyObject with a keys() method
     * @param keys the result of other's keys() method
     */
    private void mergeFromKeys(PyObject other, PyObject keys) {
        for (PyObject key : keys.asIterable()) {
            __setitem__(key, other.__getitem__(key));
        }
    }

    /**
     * Merge any iterable object producing iterable objects of length
     * 2 into this dict.
     *
     * @param other another PyObject
     */
    private void mergeFromSeq(PyObject other) {
        PyObject pairs = other.__iter__();
        PyObject pair;

        for (int i = 0; (pair = pairs.__iternext__()) != null; i++) {
            try {
                pair = PySequence.fastSequence(pair, "");
            } catch(PyException pye) {
                if (pye.match(Py.TypeError)) {
                    throw Py.TypeError(String.format("cannot convert dictionary update sequence "
                                                     + "element #%d to a sequence", i));
                }
                throw pye;
            }
            int n;
            if ((n = pair.__len__()) != 2) {
                throw Py.ValueError(String.format("dictionary update sequence element #%d "
                                                  + "has length %d; 2 is required", i, n));
            }
            __setitem__(pair.__getitem__(0), pair.__getitem__(1));
        }
    }

    /**
     * Return this[key] if the key exist, otherwise insert key with a None value and return None.
     *
     * @param key
     *            the key to lookup in the mapping.
     */
    public PyObject setdefault(PyObject key) {
        return setdefault(key, Py.None);
    }

    /**
     * Return this[key] if the key exist, otherwise insert key with the value of failobj and return
     * failobj
     *
     * @param key
     *            the key to lookup in the mapping.
     * @param failobj
     *            the default value to insert in the mapping if key does not already exist.
     */
    public PyObject setdefault(PyObject key, PyObject failobj) {
        Object internedKey = (key instanceof PyString) ? ((PyString)key).internedString() : key;
        PyObject oldValue = table.putIfAbsent(internedKey, failobj);
        if (oldValue == null) {
            return failobj;
        } else {
            return oldValue;
        }
    }

    /**
     * Return a random (key, value) tuple pair and remove the pair from the mapping.
     */
    public PyObject popitem() {
        Iterator<Entry<Object, PyObject>> it = table.entrySet().iterator();
        if (!it.hasNext()) {
            throw Py.KeyError("popitem(): dictionary is empty");
        }
        PyTuple tuple = itemTuple(it.next());
        it.remove();
        return tuple;
    }

    // not correct - we need to determine size and remove at the same time!
    public PyObject pop(PyObject key) {
        if (table.size() == 0) {
            throw Py.KeyError("pop(): dictionary is empty");
        }
        return pop(key, null);
    }

    public PyObject pop(PyObject key, PyObject failobj) {
        PyObject value = table.remove(pyToKey(key));
        if (value == null) {
            if (failobj == null) {
                throw Py.KeyError(key.__repr__().toString());
            } else {
                return failobj;
            }
        }
        return value;
    }

    /**
     * Return a copy of the mappings list of (key, value) tuple pairs.
     */
    public PyList items() {
        return new PyList(iteritems());
    }

    private PyTuple itemTuple(Entry<Object, PyObject> entry) {
        return new PyTuple(keyToPy(entry.getKey()), entry.getValue());
    }

    /**
     * Return a copy of the mappings list of keys. We have to take in account that we could be
     * storing String or PyObject objects
     */
    public PyList keys() {
        PyObject[] keyArray = new PyObject[table.size()];
        int i = 0;
        for (Object key : table.keySet()) {
            keyArray[i++] = keyToPy(key);
        }
        return new PyList(keyArray);
    }

    /**
     * Return a copy of the mappings list of values.
     */
    public PyList values() {
        return new PyList(table.values());
    }

    /**
     * return an iterator over (key, value) pairs
     */
    public PyObject iteritems() {
        return new ItemsIter(table.entrySet());
    }

    /**
     * return an iterator over the keys
     */
    // Python allows one to change the dict while iterating over it,
    // including deletion. Java does not. Can we resolve with CHM?
    public PyObject iterkeys() {
        return new KeysIter(table.keySet());
    }

    /**
     * return an iterator over the values
     */
    public PyObject itervalues() {
        return new ValuesIter(table.values());
    }

    private abstract class StringMapIter<T> extends PyIterator {

        protected final Iterator<T> iterator;

        private final int size;

        public StringMapIter(Collection<T> c) {
            iterator = c.iterator();
            size = c.size();
        }

        public PyObject __iternext__() {
            if (table.size() != size) {
                throw Py.RuntimeError("dictionary changed size during iteration");
            }
            if (!iterator.hasNext()) {
                return null;
            }
            return stringMapNext();
        }

        protected abstract PyObject stringMapNext();
    }

    private class ValuesIter extends StringMapIter<PyObject> {

        public ValuesIter(Collection<PyObject> c) {
            super(c);
        }

        public PyObject stringMapNext() {
            return iterator.next();
        }
    }

    private class KeysIter extends StringMapIter<Object> {

        public KeysIter(Set<Object> s) {
            super(s);
        }

        protected PyObject stringMapNext() {
            return keyToPy(iterator.next());
        }
    }

    private class ItemsIter extends StringMapIter<Entry<Object, PyObject>> {

        public ItemsIter(Set<Entry<Object, PyObject>> s) {
            super(s);
        }

        public PyObject stringMapNext() {
            return itemTuple(iterator.next());
        }
    }

    private static PyObject keyToPy(Object objKey){
        if (objKey instanceof String) {
            return PyString.fromInterned((String)objKey);
        } else {
            return (PyObject)objKey;
        }
    }

    private static Object pyToKey(PyObject pyKey) {
        if (pyKey instanceof PyString) {
            return ((PyString)pyKey).internedString();
        } else {
            return pyKey;
        }
    }

    public int hashCode() {
        throw Py.TypeError(String.format("unhashable type: '%.200s'", getType().fastGetName()));
    }
}
