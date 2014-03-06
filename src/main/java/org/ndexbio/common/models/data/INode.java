package org.ndexbio.common.models.data;

import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;

public interface INode extends IMetadataObject
{
    @Property("jdexId")
    public void setJdexId(String jdexId);

    @Property("jdexId")
    public String getJdexId();

    @Property("name")
    public String getName();

    @Property("name")
    public void setName(String name);

    @Adjacency(label = "nodeRepresents")
    public void setRepresents(ITerm term);

    @Adjacency(label = "nodeRepresents")
    public ITerm getRepresents();
    
    @Adjacency(label = "nodeUnificationAliases")
    public void addAlias(ITerm term);

    @Adjacency(label = "nodeUnificationAliases")
    public Iterable<ITerm> getAliases();
    
    @Adjacency(label = "nodeUnificationAliases")
    public void removeAlias(ITerm term);
    
    @Adjacency(label = "nodeRelationshipAliases")
    public void addRelatedTerm(ITerm term);

    @Adjacency(label = "nodeRelationshipAliases")
    public Iterable<ITerm> getRelatedTerms();
    
    @Adjacency(label = "nodeRelationshipAliases")
    public void removeRelatedTerm(ITerm term);

    @Adjacency(label = "nodeSupports")
    public void addSupport(ISupport support);

    @Adjacency(label = "nodeSupports")
    public Iterable<ISupport> getSupports();
    
    @Adjacency(label = "nodeSupports")
    public void removeSupport(ISupport support);
    
    @Adjacency(label = "nodeCitations")
    public void addCitation(ICitation citation);

    @Adjacency(label = "nodeCitations")
    public Iterable<ICitation> getCitations();
    
    @Adjacency(label = "nodeCitations")
    public void removeCitation(ICitation citation);
}
