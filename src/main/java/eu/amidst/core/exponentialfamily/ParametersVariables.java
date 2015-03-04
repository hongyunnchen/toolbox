/**
 ******************* ISSUE LIST **************************
 *
 * 1. Remove method getVariableByVarID()!!
 *
 * ********************************************************
 */

package eu.amidst.core.exponentialfamily;

import eu.amidst.core.datastream.Attribute;
import eu.amidst.core.datastream.Attributes;
import eu.amidst.core.variables.*;
import eu.amidst.core.variables.stateSpaceTypes.FiniteStateSpace;
import eu.amidst.core.variables.stateSpaceTypes.RealStateSpace;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by afa on 02/07/14.
 */
public class ParametersVariables implements Iterable<Variable>, Serializable {

    private static final long serialVersionUID = 5077959998533923231L;

    private List<Variable> allVariables;

    private Map<String, Integer> mapping;

    Attributes attributes;

    StaticVariables staticVariables;

    DynamicVariables dynamicVariables;

    int baseIndex;

    public ParametersVariables(StaticVariables staticVariables_) {
        this.allVariables = new ArrayList<>();
        this.mapping = new ConcurrentHashMap<>();
        this.staticVariables=staticVariables_;
        this.baseIndex=this.staticVariables.getNumberOfVars();
    }

    public ParametersVariables(DynamicVariables dynamicVariables_) {
        this.allVariables = new ArrayList<>();
        this.mapping = new ConcurrentHashMap<>();
        this.dynamicVariables=dynamicVariables_;
        this.baseIndex=this.dynamicVariables.getNumberOfVars();
    }

    public Variable newGaussianParameter(String name) {
        return this.newVariable(name, DistributionTypeEnum.NORMAL_PARAMETER, new RealStateSpace());
    }

    public Variable newInverseGammaParameter(String name){
        return this.newVariable(name, DistributionTypeEnum.INV_GAMMA_PARAMETER, new RealStateSpace());
    }

    public Variable newDirichletParameter(String name, int nOfStates) {
        return this.newVariable(name, DistributionTypeEnum.DIRICHLET_PARAMETER, new FiniteStateSpace(nOfStates));
    }

    private Variable newVariable(String name, DistributionTypeEnum distributionTypeEnum, StateSpaceType stateSpaceType) {
        VariableBuilder builder = new VariableBuilder();
        builder.setName(name);
        builder.setDistributionType(distributionTypeEnum);
        builder.setStateSpaceType(stateSpaceType);
        builder.setObservable(false);

        return this.newVariable(builder);
    }

    private Variable newVariable(VariableBuilder builder) {
        ParameterVariable var = new ParameterVariable(builder, this.baseIndex + allVariables.size());
        if (mapping.containsKey(var.getName())) {
            throw new IllegalArgumentException("Attribute list contains duplicated names: " + var.getName());
        }
        this.mapping.put(var.getName(), var.getVarID());
        allVariables.add(var);
        return var;

    }

    //public List<Variable> getListOfVariables() {
    //    return this.allVariables;
    //}

    public Variable getVariableById(int varID) {
        return this.allVariables.get(varID - this.baseIndex);
    }

    public Variable getVariableByName(String name) {
        Integer index = this.mapping.get(name);
        if (index==null) {
            throw new UnsupportedOperationException("Variable " + name + " is not part of the list of Variables");
        }
        else {
            return this.getVariableById(index.intValue());
        }
    }

    public int getNumberOfVars() {
        return this.allVariables.size();
    }

    @Override
    public Iterator<Variable> iterator() {
        return this.allVariables.iterator();
    }

    public void block(){
        this.allVariables = Collections.unmodifiableList(this.allVariables);
    }

    public List<Variable> getListOfVariables(){
        return this.allVariables;
    }

    //TODO Implements hashCode method!!

    private static class ParameterVariable implements Variable, Serializable {

        private static final long serialVersionUID = 4656207896676444152L;

        private String name;
        private int varID;
        private boolean observable;
        private StateSpaceType stateSpaceType;
        private DistributionTypeEnum distributionTypeEnum;
        private DistributionType distributionType;

        private Attribute attribute;
        private int numberOfStates = -1;


        public ParameterVariable(VariableBuilder builder, int varID) {
            this.name = builder.getName();
            this.varID = varID;
            this.observable = builder.isObservable();
            this.stateSpaceType = builder.getStateSpaceType();
            this.distributionTypeEnum = builder.getDistributionType();
            this.attribute = builder.getAttribute();

            if (this.getStateSpaceType().getStateSpaceTypeEnum() == StateSpaceTypeEnum.FINITE_SET) {
                this.numberOfStates = ((FiniteStateSpace) this.stateSpaceType).getNumberOfStates();
            }

            this.distributionType=distributionTypeEnum.newDistributionType(this);
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public int getVarID() {
            return varID;
        }

        @Override
        public boolean isObservable() {
            return observable;
        }

        @Override
        public <E extends StateSpaceType> E getStateSpaceType() {
            return (E) stateSpaceType;
        }

        @Override
        public DistributionTypeEnum getDistributionTypeEnum() {
            return distributionTypeEnum;
        }

        @Override
        public <E extends DistributionType> E getDistributionType() {
            return (E)this.distributionType;
        }

        @Override
        public boolean isTemporalClone() {
            throw new UnsupportedOperationException("In a static context a variable cannot be temporal.");
        }

        @Override
        public Attribute getAttribute() {
            return attribute;
        }

        @Override
        public boolean isDynamicVariable() {
            return false;
        }

        @Override
        public boolean isParameterVariable() {
            return true;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()){
                return false;
            }

            Variable var = (Variable) o;

            return this.getVarID()==var.getVarID();
        }

        @Override
        public int getNumberOfStates() {
            return this.numberOfStates;
        }

        @Override
        public int hashCode(){
            return this.name.hashCode();
        }


    }
}
