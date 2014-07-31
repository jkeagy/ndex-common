package org.ndexbio.common.models.object.network;

import java.util.List;

import org.ndexbio.common.exceptions.NdexException;

public class RawCitation implements Comparable <RawCitation>{
	
	private String title;
	private List<String> contributors;
	private String idType;   // pubmed or DOI etc...
	private String identifier;
	
	public RawCitation (String title, String idType, String identifier, List<String> contributors) throws NdexException {
		this.title = title;
		this.setContributors(contributors);
		this.setIdType(idType);
		this.setIdentifier(identifier);
		if ( title == null && identifier == null)
			throw new NdexException ("Invalid Citation object: title and identifier are both null.");
	}
	
	 

	@Override
	public int compareTo(RawCitation o) {
        if ( identifier == null ) {
        	if (o.getIdentifier() != null)
        		return -1;
        } else {
            if (o.getIdentifier() == null) return 1;
        
            int c = identifier.compareTo(o.getIdentifier());
            if (c !=0) return c;
        }
        
        if ( idType == null ) {
        	 if ( o.getIdType() != null)
        		return -1;
        } else {
            if ( o.getIdType() == null) return 1;
        
	   	    int c =idType.compareTo(o.getIdType());
		    if ( c != 0 ) return c;
		    if ( identifier != null )
		    	return 0;
        }

        if (title == null) {
        	if ( o.getTitle() != null) return -1;
        } else {
        	if ( o.getTitle() == null) return 1;
    		int c = title.compareTo(o.getTitle());
    		if ( c!=0) return c;
        }

		return 0;
	}


	@Override
	public int hashCode() {
		if (identifier !=null)
			return this.identifier.hashCode();
		  return title.hashCode();
		
	}
	
	@Override
	public boolean equals (Object o) {
		if (o instanceof RawCitation)
			return compareTo((RawCitation)o) == 0;
		return false;
	}

	public String getTitle() {
		return title;
	}


/*
	public void setTitle(String title) {
		this.title = title;
	}

*/

	public List<String> getContributors() {
		return contributors;
	}



	public void setContributors(List<String> contributors) {
		this.contributors = contributors;
	}



	public String getIdType() {
		return idType;
	}



	public void setIdType(String idType) {
		this.idType = idType;
	}



	public String getIdentifier() {
		return identifier;
	}



	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

}