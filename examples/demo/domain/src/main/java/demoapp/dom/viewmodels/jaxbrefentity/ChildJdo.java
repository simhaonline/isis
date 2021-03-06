package demoapp.dom.viewmodels.jaxbrefentity;

import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.isis.applib.annotation.Bounding;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.applib.jaxbadapters.PersistentEntityAdapter;

import lombok.Getter;
import lombok.Setter;

//tag::class[]
@PersistenceCapable(identityType = IdentityType.DATASTORE, schema = "demo" )
@DatastoreIdentity(strategy = IdGeneratorStrategy.IDENTITY, column = "id")
@DomainObject(bounding = Bounding.BOUNDED)
@XmlJavaTypeAdapter(PersistentEntityAdapter.class)
public class ChildJdo {

    public ChildJdo(String name) {
        this.name = name;
    }

    @Title
    @Getter @Setter
    private String name;
}
//end::class[]
