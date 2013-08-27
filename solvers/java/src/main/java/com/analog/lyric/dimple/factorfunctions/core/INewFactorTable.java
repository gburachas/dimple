package com.analog.lyric.dimple.factorfunctions.core;

import java.util.BitSet;

import com.analog.lyric.dimple.model.DiscreteDomainListConverter;


public interface INewFactorTable extends INewFactorTableBase, IFactorTable
{
	/**
	 * Removes entries from sparse representation of table that have zero weights.
	 * <p>
	 * This may affect the {@link #sparseSize()} and the relationship between sparse and
	 * joint indexes.
	 * <p>
	 * @return the number of sparse entries that were removed.
	 */
	public int compact();
	
	@Override
	public INewFactorTable convert(DiscreteDomainListConverter converter);
	
	public NewFactorTableRepresentation getRepresentation();

	/**
	 * Sets representation to {@link NewFactorTableRepresentation#DENSE_ENERGY} with
	 * provided energies.
	 * @param energies specifies the energies of the table in dense joint-index order. Must have length
	 * equal to {@link #getDomainList()}.getCardinality().
	 */
	public void setEnergiesDense(double[] energies);
	
	/**
	 * Sets representation to {@link NewFactorTableRepresentation#DENSE_WEIGHT} with
	 * provided weights.
	 * @param weights specifies the weights of the table in dense joint-index order. Must have length
	 * equal to {@link #getDomainList()}.getCardinality().
	 */
	public void setWeightsDense(double[] weights);

	public void setDirected(BitSet outputSet);
	
	public void setRepresentation(NewFactorTableRepresentation representation);
	
	/**
	 * Sets representation to {@link NewFactorTableRepresentation#SPARSE_ENERGY} with
	 * provided energies for each joint index.
	 * <p>
	 * @param jointIndices are the joint indexes of the entries to put in the table.
	 * @param energies specifies the energies of the table in the same order as {@code jointIndices}.
	 * @throws IllegalArgumentException if {@code jointIndices} and {@code energies} have different lengths,
	 * if there are duplicate indices or any of the indices is not in a valid range for the table.
	 */
	public void setEnergiesSparse(int[] jointIndices, double[] energies);
	
	/**
	 * Sets representation to {@link NewFactorTableRepresentation#SPARSE_WEIGHT} with
	 * provided weights for each joint index.
	 * <p>
	 * @param jointIndices are the joint indexes of the entries to put in the table.
	 * @param weights specifies the weights of the table in the same order as {@code jointIndices}.
	 * @throws IllegalArgumentException if {@code jointIndices} and {@code energies} have different lengths,
	 * if there are duplicate indices or any of the indices is not in a valid range for the table.
	 */
	public void setWeightsSparse(int[] jointIndices, double[] weights);
}