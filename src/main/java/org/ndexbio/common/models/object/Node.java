package org.ndexbio.common.models.object;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.ndexbio.common.models.data.IEdge;
import org.ndexbio.common.models.data.INode;
import org.ndexbio.common.models.data.ITerm;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Node extends MetadataObject
{
    private String _name;
    private String _represents;
    private List<String> _aliases;
    private List<String> _relatedTerms;
    
    
    
    /**************************************************************************
    * Default constructor.
    **************************************************************************/
    public Node()
    {
        super();
    }
    
    /**************************************************************************
    * Populates the class (from the database) and removes circular references.
    * 
    * @param node The Node with source data.
    **************************************************************************/
    public Node(INode node)
    {
        super(node);

        
        _name = node.getName();
        _relatedTerms = new ArrayList<String>();
        _aliases = new ArrayList<String>();
        
        if (node.getRepresents() != null)
            _represents = node.getRepresents().getJdexId();
        
        for (final ITerm iTerm : node.getAliases())
            _aliases.add(iTerm.getJdexId());
        
        for (final ITerm iTerm : node.getRelatedTerms())
            _relatedTerms.add(iTerm.getJdexId());
    }
    
    
    
    public String getName()
    {
        return _name;
    }
    
    public void setName(String name)
    {
        _name = name;
    }
    
    public String getRepresents()
    {
        return _represents;
    }
    
    public void setRepresents(String representsId)
    {
        _represents = representsId;
    }

	public List<String> getAliases() {
		return _aliases;
	}

	public void setAliases(List<String> _aliases) {
		this._aliases = _aliases;
	}

	public List<String> getRelatedTerms() {
		return _relatedTerms;
	}

	public void setRelatedTerms(List<String> _relatedTerms) {
		this._relatedTerms = _relatedTerms;
	}
    
    
}
