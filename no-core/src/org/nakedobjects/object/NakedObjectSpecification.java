package org.nakedobjects.object;

import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.object.reflect.NakedObjectField;


public interface NakedObjectSpecification {
    void clearDirty(NakedObject object);

    Action getClassAction(Action.Type type, String name);

    Action getClassAction(Action.Type type, String name, NakedObjectSpecification[] parameters);

    Action[] getClassActions(Action.Type type);

    Hint getClassHint();
    
    Object getExtension(Class cls);
    
    Class[] getExtensions();

    NakedObjectField getField(String name);
    
    Object getFieldExtension(String name, Class cls);

    Class[] geFieldExtensions(String name);
    
    NakedObjectField[] getFields();

    /**
     * Returns the name of the NakedClass. This is the fully qualified name of
     * the Class object that this object represents (i.e. it includes the
     * package name).
     */
    String getFullName();

    Action getObjectAction(Action.Type type, String name);

    Action getObjectAction(Action.Type type, String name, NakedObjectSpecification[] parameters);

    Action[] getObjectActions(Action.Type type);

    /**
     * Returns the short name (with spacing) for this object in a pluralised
     * form. The plural from is obtained from the defining classes pluralName
     * method, if it exists, or by adding 's', 'es', or 'ies dependending of the
     * name's ending.
     */
    String getPluralName();

    /**
     * Returns the class name without the package. Removes the text up to, and
     * including the last period (".").
     */
    String getShortName();

    /**
     * Returns the short name (with spacing) of this NakedClass object. This is
     * the objects name with package name removed.
     * 
     * <p>
     * Removes the text up to, and including the last period (".").
     * </p>
     */
    String getSingularName();

    NakedObjectField[] getVisibleFields(NakedObject object);

    boolean hasSubclasses();

    NakedObjectSpecification[] interfaces();

    boolean isAbstract();

    boolean isDirty(NakedObject object);

    boolean isLookup();

    boolean isObject();

    /**
     * Determines if this specification respresents the same specification, or a
     * subclass, of the specified specification.
     */
    boolean isOfType(NakedObjectSpecification cls);

    /**
     * Determines if objects of this specification can be persisted or not. If
     * it can be persisted (i.e. it return something other than
     * Persistable.TRANSIENT) NakedObject.isPersistent() will indicated whether
     * the object is persistent or not. If they cannot be persisted then
     * NakedObject.persistable() should be ignored.
     * 
     * @see NakedObject#persistable()
     */
    Persistable persistable();

    boolean isValue();

    void markDirty(NakedObject object);

    NakedObjectSpecification[] subclasses();

    NakedObjectSpecification superclass();

    void deleted(NakedObject object);

    String getTitle(NakedObject naked);
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
 * Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address
 * of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking GU21
 * 1NR, UK).
 */