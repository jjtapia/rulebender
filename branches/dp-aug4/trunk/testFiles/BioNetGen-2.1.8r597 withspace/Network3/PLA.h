//#ifndef PLA_H
//#define PLA_H

#include "network.h"

/* Return the rate of rxn -- for internal use only */
static double rxn_rate( Rxn *rxn, double *X, int discrete){
	double rate;
	int *iarr, *index;
	int ig, i1, i2, n_denom;
	int q;
	double *param,x, xn, kn;
	double St, Et, kcat, Km, S, b;

	/* Don't calculate rate of null reactions */
	if (!rxn) return(0.0);

	++network.n_rate_calls;

	switch(rxn->rateLaw_type){

	  case ELEMENTARY:
	    // v= k1*X1...Xn
	    rate= rxn->stat_factor*rxn->rateLaw_params[0];
	    iarr=rxn->r_index;
	    // Handle reactions with discrete molecules with multiple copies of the same reactants.
	    // NOTE: Will only apply correct formula if repeated species are grouped together (which is done
	    //       automatically by BNG).
	    if (discrete){
	    	double n=0.0;
//	    	rate*= X[*iarr]; // NOTE: This assumes at least one reactant species (no zeroth-order rxns)
//	        for (index=iarr+1; index<iarr+rxn->n_reactants; ++index){
	    	for (index=iarr; index<iarr+rxn->n_reactants; ++index){
	    		if (index > iarr){
					if (*index == *(index-1)){
						n+= 1.0;
					}
					else {
						n=0.0;
					}
	    		}
	        	rate*= (X[*index]-n);
	        }
	    }
	    // Continuous case
	    else {
	      for (index=iarr; index<iarr+rxn->n_reactants; ++index){
	    	  rate*= X[*index];
	      }
	    }
	    break;

	case MICHAELIS_MENTEN:
		/* Second rate, if present, is Michaelis-Menten Km */
		St= X[rxn->r_index[0]];
		kcat= rxn->rateLaw_params[0];
		Km= rxn->rateLaw_params[1];
		/* S + E + ... -> P + E + .. */
		for(q=1,Et=0; q<rxn->n_reactants;++q){
			Et += X[rxn->r_index[q]];
		}
		b= St-Km-Et;
		S= 0.5*(b + sqrt(b*b+4.0*St*Km));
		rate= rxn->stat_factor*kcat*Et*S/(Km+S);
		break;

	case SATURATION:
		param= rxn->rateLaw_params;
		/* if dim(param)==1
			 rate= stat_factor*param[0], a zeroth order rate law
		   else
			 rate = stat_factor*param[0]*R1...Rn/((R1+param[1])*(R2+param[2])...
		   where terms in denominator are only calculated if param[n] is defined
		*/
		rate= rxn->stat_factor*param[0];
		iarr=rxn->r_index;
		++param;
		n_denom= rxn->n_rateLaw_params-1;
		if (n_denom>0){
		    // Handle reactions with discrete molecules with multiple copies of the same reactants.
		    // NOTE: Will only apply correct formula if repeated species are grouped together (which is done
		    //       automatically by BNG).
			if (discrete){
				double n = 0.0;
				/* Compute contributions to rate from species appearing in both numerator
				   and denominator */
				for (ig=0; ig<n_denom; ++ig){
					if (ig > 0){
						if (iarr[ig] == iarr[ig-1]){
							n += 1.0;
						}
						else{
							n = 0.0;
						}
					}
					x= X[iarr[ig]];
					rate*= (x-n)/(param[ig]+x);
				}
				/* Compute contributions to rate from species appearing only in numerator */
				for (ig=n_denom; ig<rxn->n_reactants; ++ig){
					if (iarr[ig] == iarr[ig-1]){
						n += 1.0;
					}
					else{
						n = 0.0;
					}
					rate*= (X[iarr[ig]]-n);
				}
			}
			// Continuous case
			else{
				/* Compute contributions to rate from species appearing in both numerator
				   and denominator */
				for (ig=0; ig<n_denom; ++ig){
					x= X[iarr[ig]];
					rate*= x/(param[ig]+x);
				}

				/* Compute contributions to rate from species appearing only in numerator */
				for (ig=n_denom; ig<rxn->n_reactants; ++ig){
					rate*= X[iarr[ig]];
				}
			}
		}
		break;

	case HILL:
		param= rxn->rateLaw_params;
		iarr=rxn->r_index;
		x= X[iarr[0]];
		xn= pow(x,param[2]);
		kn= pow(param[1],param[2]);
		rate= rxn->stat_factor*param[0]*xn/(kn+xn);
	    // Handle reactions with discrete molecules with multiple copies of the same reactants.
	    // NOTE: Will only apply correct formula if repeated species are grouped together (which is done
	    //       automatically by BNG).
	    if (discrete){
	    	double n=0.0;
			/* Compute contributions to rate from species appearing only in numerator */
	        for (ig=1; ig<rxn->n_reactants; ++ig){
	        	if (iarr[ig] == iarr[ig-1]){
	        		n+= 1.0;
	        	} else {
	        		n=0.0;
	        	}
	        	rate*= (X[iarr[ig]]-n);
	        }
	    }
	    // Continuous case
		else {
			/* Compute contributions to rate from species appearing only in numerator */
			for (ig=1; ig<rxn->n_reactants; ++ig){
				rate*= X[iarr[ig]];
			}
		}
		break;

	case FUNCTION:
		// v= k1*X1...Xn
		rate= rxn->stat_factor*network.rates->elt[ rxn->rateLaw_indices[0] - 1]->val ;
//		rate= rxn->stat_factor*rxn->rateLaw_params[0];
//		cout << "ratelaw param is " << network.rates->elt[ rxn->rateLaw_indices[0] - 1]->val << endl;
		iarr=rxn->r_index;
	    // Handle reactions with discrete molecules with multiple copies of the same reactants.
	    // NOTE: Will only apply correct formula if repeated species are grouped together (which is done
	    //       automatically by BNG).
	    if (discrete){
	    	double n=0.0;
	    	rate*= X[*iarr];
	        for (index=iarr+1; index<iarr+rxn->n_reactants; ++index){
	        	if (*index == *(index-1)){
	        		n+= 1.0;
	        	} else {
	        		n=0.0;
	        	}
	        	rate*= (X[*index]-n);
	        }
	    }
	    // Continuous case
		else {
			for (index=iarr; index<iarr+rxn->n_reactants; ++index){
				rate*= X[*index];
			}
		}
		break;
	}

	return(rate);
}

class rule{
public: 
	rule(string _name, int _index, vector<string> r_strings, vector<string> p_strings, string rl, vector<int> _rxns_indices,
			map<string, int> *param_map, map<string, int> *observ_map, vector<double> *x, double *x_offset);

	string			name;
	int     		index;
	vector<int>		r_indices;	// indices of groups/observables, not species
	vector<int> 	p_indices; 	// ditto
	int      		rl_type; 	// rate law type
	int 			type; 		// "rxn" type, ES POISSON etc.
	double 	  		tau;
	vector<double>  rateLaw_params;
	vector<int>     rateLaw_indices;
	vector<int>     rxns_indices; // list of reactions that rule compiles into
	double 	  		propensity;
	double 	  		old_const;
	double 	  		base_a;
	vector<double>  r_stoich;
	vector<double>  p_stoich;
	//vector<vector<double> >  r_stoich; // pseudo stoich vector, index is reactant (base 0) in rule, and value is vector of number
										 // molecules formed when that reactant is consumed
	vector<vector<mu::Parser> > p_stoich_eqs; 	// first vector is index by rule's 'i'th product group number, second vector's int
												// is index by group's 'j'th species number. The parser is an equation
												// to calculate that group's contribution to that species's p_stoich entry
}; 

class rxn{
public:
	rxn() {}
	rxn(double new_propensity, vector<int> new_r_stoich, vector<int> new_p_stoich) {
		propensity = new_propensity;
		r_stoich = new_r_stoich;
		p_stoich = new_p_stoich;
		for (int i = 0; i < r_stoich.size(); i++)
			stoich.push_back( p_stoich[i] - r_stoich[i] );
		old_const = numeric_limits<double>::infinity();
		tau = numeric_limits<double>::infinity();
	}

	double propensity;
	vector<int> r_stoich;
	vector<int> p_stoich;
	vector<int> stoich;
	double tau;
	double beta;
	int type;
	double old_const; // holds a' * (tau_ES' - tau')

	friend class pla;
};

class pla {
public: 
	pla(double eps, double e_time, int type);
	// main tau leaping method
	void simulate(int tau_comp_type, double *new_sample_times);
	void simulate_rules(double *new_sample_times);
	void test();

//private: 

	//void simulate_loop(vector<int>&, double&, double*&, int&, int&, int&);
	//void simulate_loop_1(vector<int>& ES_rxns, double& min_tau_ES);

	pair<int, double> find_min_tau(vector<pair<int, int> > rxns_fired);
	pair<int, double> find_min_tau();
	// methods
	void read_rules();
	double compute_tau(int type, double old_tau);
	vector<int> classify_rxns(double);
	vector<int> reclassify_rxns(double tau);
	vector<int> next_rxns();

	// calculate derivative of propensity of rxn rxn_index with respect to concentration of species species_index
	// aproximated as the difference between a_j(x, x_i+1) and a_j(x)
	double deriv(int irxn, int i);
	map<string, vector<int> > get_rule_rxns_map();
	map<string, int> get_param_map();
	map<string, int> get_observ_map();
	map<int, vector<int> > species_group_map; // both species and groups are indexed on base 1

	void create_dependency_lists();
	void update_concentrations_TL(int irxn, int n, int);
	void redo_update_concentrations(int irxn, int n);
	void update_propensities(vector<pair<int, int> >);
	void update_propensities(int);
	bool *rxn_included;
	bool *func_included;
	bool *observ_included;

	void update_propensities_rules(vector<int>);
	vector<int> calc_rxns_to_update (vector<int> observ_changed);
	vector<stack<int> > dependency_path(int irxn, int i);
	vector<pair<int, int> > choose_rxns(double tau, int);
	vector<pair<int, int> > distribute_firings( vector<pair<int, int> >  );
	void postLeap_fix(vector<pair<int, int> > &, double &, int, int&);
	bool check_propensities(double);
	void initialize_rule_stoich();
	bool check_for_neg_c();
	void set_tau_mu(double tau, vector<int>);
	bool all_ES(int, vector<int>);
	double calc_min_tau_ES ();
	void update_x_g(int);
	double *rxn_fire_count;
	//double *sample_times;

	//int last_ES_fired;
	//double p;  // factor by which to decrease tau in postleap check if tau was rejected, between 0 and 1
	//double _p; // factor by which to decrease tau in postleap check if tau was accepted for epsilon, but rejected for 3/4 epsilon
	//double q;  // factor by which to raise tau in postleap check if tau was accepted for epsilon and for 3/4 epsilon
	//int count;
	int sim_type; // type of simulation
	double sys_time; // current time
	double epsilon;
	double a_tot;
	double curr_tau, prev_tau;
	double stop_time; // stop time
	int n_sample;
	vector<rxn> rxns; // Rxn vector, indexed same as rxns in Network
	vector<double> x; // Concentrations vector, indexed same as species elt_array in Network
	vector<double> x_old;
	vector<double> x_g;
	vector<rule> rules;
	double* g;
	double* x_offset, *x_offset_old, *x_faux;
	double* delta_a, *delta_a_p, *pre_update_a;
	ofstream TL_out, PLA_out, TL_out_cdat, a_out;
	gsl_rng* r;
	stringstream buf;
	vector<int> _props_to_check;
	int num_rxns_in_postleap, num_rxns_fired_tot;
	int num_zero_rxns;
	//pair<int, int> ES_rxn_just_fired;
	int ES_rxn_just_fired;
	vector<int> ES_rxns;	// keeps track of rxns that are active and ES
	list<int> active_rxns; 	// keeps track of rxns that are active
	map<int, list<int>::iterator> active_rxns_map; // maps 0 based rxn index to iterator in list
	vector<bool> isActive;

	void check_all_ES(vector<pair<int, int> >);
	void print_pops();
	void print_nonzero_propensities();
	void print_delta_a();

	// dependency structures
	// reactants -> observables
	vector<int> * react_observ_affect;	// reactancts by observables
	// observables -> functions
	vector<int> * observ_func_affect; 	// observables by functions
	// functions -> parameters
	vector<int> func_param_affect; 		// functions by parameters (n x 1)
	// parameters -> functions
	vector<int> * param_func_affect; 	// parameters by functions
	// parameters -> rxns
	vector<int> * param_rxn_affect; 	// parameters by reactions
	// rxns -> observables
	vector<int> * rxn_observ_affect; 	// reactions by observables
	// parameters -> rules
	vector<int> * param_rule_affect; 	// parameters (variable) by rules
	// observables -> rules
	vector<int> * observ_rule_affect; 	// observables by rules

	// rxns -> rxns
	// based on stoichiometry, update list of reactions
	// rxns are indexed in base 0
	vector<int> * rxn_rxn_affect;
}; 

//#endif
