package org.cad.interruptus.entity;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;
import java.util.Map;
import java.util.Objects;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement
@ApiModel(value = "Type", description = "Type resource representation")
public class Type implements Entity
{
    @ApiModelProperty(value = "List of properties", required = true)
    protected Map<String, String> properties;
    
    @ApiModelProperty(value = "Type unique name", required = true)
    protected String name;

    @ApiModelProperty(value = "Type inventory hierarchy", required = false)
    protected String hierarchy;

    public Type()
    {
    }

    public Type(final String name, final Map<String, String> properties)
    {
        this.name       = name;
        this.properties = properties;
    }

    @Override
    @XmlTransient
    public String getId()
    {
        return name;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Map<String, String> getProperties()
    {
        return properties;
    }

    public void setProperties(Map<String, String> properties)
    {
        this.properties = properties;
    }

    public void setProperty(final String propertyName, final String propertyType)
    {
        this.properties.put(propertyName, propertyType);
    }

    public String getHierarchy()
    {
        return hierarchy;
    }

    public void setHierarchy(final String hierarchy)
    {
        this.hierarchy = hierarchy;
    }

    @Override
    public String toString()
    {
        return String.format("{name:'%s', hierarchy:'%s', properties:%s}", name, hierarchy, properties);
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash     = 13 * hash + Objects.hashCode(this.properties);
        hash     = 13 * hash + Objects.hashCode(this.hierarchy);
        hash     = 13 * hash + Objects.hashCode(this.name);

        return hash;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (obj == null) {
            return false;
        }

        if ( ! getClass().equals(obj.getClass())) {
            return false;
        }

        final Type other = (Type) obj;

        if ( ! Objects.equals(this.name, other.name)) {
            return false;
        }
        
        if ( ! Objects.equals(this.hierarchy, other.hierarchy)) {
            return false;
        }

        return Objects.equals(this.properties, other.properties);
    }
}
