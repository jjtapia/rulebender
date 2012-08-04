#include "PLA.h"

int num_rejected = 0; 
int num_accepted = 0; 
int num_steps = 0; 

// constructor 
pla::pla (double eps, double _n_sample, int type) {
/*
  string PLA_firings_per_step = network.name; 
  PLA_firings_per_step.erase(PLA_firings_per_step.size()-4, PLA_firings_per_step.size()); 
  PLA_firings_per_step += "_PLA_firings.dat"; 
*/
	string TL_gdat_file_name = network.name;
	TL_gdat_file_name.erase(TL_gdat_file_name.size()-4, TL_gdat_file_name.size());
	string TL_cdat_file_name = TL_gdat_file_name;
	//string a_out_name = TL_gdat_file_name;
	TL_cdat_file_name += "_PLA.cdat";
	//a_out_name += "_a.rdat";
	TL_gdat_file_name += "_PLA.gdat";
/*
	PLA_out.open(PLA_firings_per_step.c_str());
	PLA_out << left << setw(10) << "time ";
	for (int i = 0; i < network.reactions->n_rxn; i++)
		PLA_out << "rxn" << left << setw(7) << i;
	PLA_out << endl;
*/
	rxn_fire_count = new double[network.reactions->n_rxn];
	for (int i = 0; i < network.reactions->n_rxn; i++)
		rxn_fire_count[i] = 0;

	TL_out.open(TL_gdat_file_name.c_str());
	TL_out_cdat.open(TL_cdat_file_name.c_str());
	//a_out.open(a_out_name.c_str());

	sys_time = 0;
	epsilon = eps;
	cout << "epsilon: " << eps << endl;
	a_tot = 0;
	n_sample = _n_sample;

	// set up concentrations
	x_offset = new double[network.species->n_elt+1];
	x_offset_old = new double[network.species->n_elt+1];
	x_faux = new double[network.species->n_elt];
	x_offset[0] = 0;
	for (int i = 0; i < network.species->n_elt; i++) {
		x.push_back( network.species->elt[i]->val );
		x_old.push_back( network.species->elt[i]->val );
		x_offset[i+1] = network.species->elt[i]->val;
		x_offset_old[i+1] = network.species->elt[i]->val;
	}

	// set up reactions (propensities and stoichiometry vectors)
	double a;
	for (int i = 0; i < network.reactions->n_rxn; i++) {
		vector<int> r_stoich, p_stoich;
		r_stoich.insert(r_stoich.begin(), network.species->n_elt, 0);
		p_stoich.insert(p_stoich.begin(), network.species->n_elt, 0);

		for (int j = 0; j < network.reactions->rxn[i]->n_reactants; j++)
			r_stoich[ network.reactions->rxn[i]->r_index[j] - 1 ]++;
		for (int j = 0; j < network.reactions->rxn[i]->n_products; j++)
			p_stoich[ network.reactions->rxn[i]->p_index[j] - 1 ]++;

		a = rxn_rate( network.reactions->rxn[i], x_offset, 1);
		rxn new_rxn( a, r_stoich, p_stoich);
		a_tot += a;
		rxns.push_back(new_rxn);
	}

//	cout << "rxns rstoich, pstoich, stoich size:" << endl;
//	for (int i = 0; i < rxns.size(); i++)
//		cout << rxns[i].r_stoich.size() << "|" << rxns[i].p_stoich.size() << "|" << rxns[i].stoich.size() << endl;

	rxn_included = new bool[rxns.size()];
	func_included = new bool[ network.functions.size() ];
	observ_included = new bool[ network.n_groups ];
/*
	for (int i = 0; i < network.n_groups; i++)
		observ_included[i] = false;
	for (int i = 0; i < rxns.size(); i++)
		rxn_included[i] = false;
*/
	delta_a = new double[rxns.size()];
	delta_a_p = new double[rxns.size()];
	for (int i = 0; i < rxns.size(); i++){
		delta_a[i] = delta_a_p[i] = 0;
	}
	pre_update_a = new double[rxns.size()];

	// calculate g for each species s[i]
	g = new double[x.size()];
	for (int i = 0; i < x.size(); i++){
		g[i] = 0;
	}
	int n_reactants;
	int reactants[3];
	for (int i = 0; i < rxns.size(); i++) {

		// for each reaction, figure out the number and indices (base 1) of reactants
		n_reactants = network.reactions->rxn[i]->n_reactants;
		for (int j = 0; j < n_reactants; j++){
			reactants[j] = network.reactions->rxn[i]->r_index[j];
		}

		// set g for each reactant accordingly
		if (n_reactants == 1){
			// s_i -> products
			if (g[reactants[0]-1] < 1)
				g[reactants[0]-1] = 1;
		}

		if (n_reactants == 2) {
			// s_i + s_j -> products
			if (rxns[i].r_stoich[reactants[0]-1] == 1) {
				// for both reactants, if g < 2, set g to 2
				if (g[reactants[0]-1] < 2)
					g[reactants[0]-1] = 2;
				if (g[reactants[1]-1] < 2)
					g[reactants[1]-1] = 2;
			}
			// 2*s_i -> products
			else
				if (g[reactants[0]-1] < 3)
					g[reactants[0]-1] = 3;
		}

		if (n_reactants == 3) {
			// reactant 1 appears once
			if (rxns[i].r_stoich[reactants[0]-1] == 1){
				// s_i + s_j + s_k -> products
				if (rxns[i].r_stoich[reactants[1]-1] == 1) {
					if (g[reactants[0]-1] < 3)
						g[reactants[0]-1] = 3;
					if (g[reactants[1]-1] < 3)
						g[reactants[1]-1] = 3;
					if (g[reactants[2]-1] < 3)
						g[reactants[2]-1] = 3;
				}
			}
			// s_i + 2*s_j -> products
			if (rxns[i].r_stoich[reactants[1]-1] == 2){
				// try to set g of first to 3 and second to 4.5
				if (g[reactants[0]-1] < 3)
					g[reactants[0]-1] = 3;
				if (g[reactants[1]-1] < 4.5)
					g[reactants[1]-1] = 4.5;
			}

			// reactant 1 appears twice
			// 2*s_i + s_j -> products
			if (rxns[i].r_stoich[reactants[0]-1] == 2){
				// try to set g of first to 4.5 and second to 3
				if (g[reactants[0]-1] < 4.5)
					g[reactants[0]-1] = 4.5;
				if (rxns[i].r_stoich[reactants[1]-1] == 1)
					if (g[reactants[1]-1] < 3)
						g[reactants[1]-1] = 3;
				if (rxns[i].r_stoich[reactants[1]-1] == 2)
					if (g[reactants[2]-1] < 3)
						g[reactants[2]-1] = 3;
			}

			// 3*s_i -> products
			if (rxns[i].r_stoich[reactants[0]-1] == 3)
				g[reactants[0]-1] = 5.5;

			// set g to 5.5
		}

		if (n_reactants > 3)
			cout << "WARNING: REACTION NUMBER " << i+1
				 << " HAS MORE THAN 3 REACTANTS. CALCULATION OF G FOR THESE REACTANTS MAY BE INCORRECT" << endl;
	}

	// set up dependency lists
	create_dependency_lists();

	// compute initial tau
	// dummy vector not used in this tau computation
	vector<int> dummy_vector;
	curr_tau = compute_tau(type, 0);
	//cout << "e " << epsilon << " t " << curr_tau << endl;

	// initialize random number
	r = gsl_rng_alloc (gsl_rng_mt19937);
	srand(time(NULL));
	gsl_rng_set (r, rand());
}

// update groups "concentration" vector using indices from network and values from object's x vector
// type = REAL(0) or FAUX(1)
void pla::update_x_g(int type) {
	if (type == REAL) {
		for(int i = 0; i < network.n_groups; i++) {
			x_g[i] = 0;
			for (int j = 0; j < network.spec_groups_vec[i]->n_elt; j++)
				x_g[i] += x[ network.spec_groups_vec[i]->elt_index[j] - 1 ]*network.spec_groups_vec[i]->elt_factor[j];
		}
	}
	else if(type == FAUX) {
		for(int i = 0; i < network.n_groups; i++) {
			x_g[i] = 0;
			for (int j = 0; j < network.spec_groups_vec[i]->n_elt; j++)
				x_g[i] += x_faux[ network.spec_groups_vec[i]->elt_index[j] - 1 ]*network.spec_groups_vec[i]->elt_factor[j];
		}
	}
}

// returns an indexical update path that connects species i to reaction irxn 
// all indices in parameters and return are base 0 
// if no path exists, return an empty vector 
// if a path exists, return vector of stacks (paths) with reverse path with form < (parameter, function)^n, observable >
vector<stack<int> > pla::dependency_path(int irxn, int i) {
	stack<int> obs;
	stack<int> fun;
	stack<int> par;
	stack<int> path;
	vector<stack<int> > result;
	int param_to_find = network.reactions->rxn[ irxn ]->rateLaw_indices[0];

	for (int j = 0; j < react_observ_affect[i].size(); j++)
		obs.push( react_observ_affect[i][j] );
	while (!obs.empty()) {
		vector<int> * par_fun = new vector<int>[network.rates->n_elt]; // parameters by functions
		for (int j = 0; j < network.rates->n_elt; j++)
			par_fun[j] = param_func_affect[j];

		path.push( obs.top() );
		for (int j = 0; j < observ_func_affect[ obs.top() - 1 ].size(); j++)
			fun.push( observ_func_affect[ obs.top() - 1 ][j] );
		// choose function from observable
		while ( !fun.empty()) {
			path.push(fun.top());
			path.push(func_param_affect[fun.top() - 1]);
			// if you found an update path, add the path to the result vector
			// then go back to the last parameter or observable try the next possible path
			if (param_to_find == path.top()) {
				result.push_back(path);
				path.pop(); // pop parameter
				path.pop(); // pop function
				fun.pop();
			}
			// pop function and parameter from stack and choose new function and parameter from observable
			else if ( par_fun[ path.top() - 1 ].empty() ) {
				path.pop(); // pop parameter
				path.pop(); // pop function
				fun.pop();
			}
			// choose another function from that parameter and push it onto path
			// this is where we start to tap the functions/parameters dependencies
			else {
				path.push( par_fun[ path.top() - 1 ][par_fun[ path.top() - 1 ].size() - 1] ); // push new function
				path.push( func_param_affect[ path.top() - 1 ] ); // push new parameter
				while (path.size() > 1) { // make sure that top of path is parameter index, not observable
					// if you found an update path, add the path to the result vector
					// then go back to the last parameter or observable try the next possible path
					if (path.top() == param_to_find) {
						result.push_back(path);
						path.pop();
						path.pop();
						par_fun[path.top() - 1].pop_back();

					}
					else if ( par_fun[path.top() - 1].empty() ) { // parameter at top does not affect any other functions
						path.pop();
						path.pop();
						par_fun[path.top() - 1].pop_back();
					}
					else { // parameter does affect other functions, so choose new function and push it
						path.push( par_fun[ path.top() - 1 ][par_fun[ path.top() - 1 ].size() - 1] ); // push new function
						path.push( func_param_affect[ path.top() - 1 ] ); // push new parameter
					}
				}
			}
		}
		path.pop();
		obs.pop();
	}
	return result;
}

void pla::create_dependency_lists() {

	/* reactants -> observables */
	react_observ_affect = new vector<int>[network.species->n_elt]; // reactancts by observables
	for (Group* curr_group = network.spec_groups; curr_group != NULL; curr_group = curr_group->next)
		for (int i = 0; i < curr_group->n_elt; i++)
			react_observ_affect[ curr_group->elt_index[i] -1 ].push_back(curr_group->index);
        
	/* rxns -> observables */
	rxn_observ_affect = new vector<int>[network.reactions->n_rxn]; //
	for (int i = 0; i < network.reactions->n_rxn; i++) {
		for (int j = 0; j < network.reactions->rxn[i]->n_reactants; j++)
			for (int k = 0; k < react_observ_affect[network.reactions->rxn[i]->r_index[j] -1].size(); k++)
				rxn_observ_affect[i].push_back( react_observ_affect[ network.reactions->rxn[i]->r_index[j] -1 ][k] );

		for (int j = 0; j < network.reactions->rxn[i]->n_products; j++)
			for (int k = 0; k < react_observ_affect[network.reactions->rxn[i]->p_index[j] -1].size(); k++)
				rxn_observ_affect[i].push_back( react_observ_affect[ network.reactions->rxn[i]->p_index[j] -1 ][k] );
		remove_redundancies(rxn_observ_affect[i]);
	}

	/* observables -> functions */
	observ_func_affect = new vector<int>[network.n_groups]; // observables by functions
	for (int i = 0; i < network.func_observ_depend.size(); i++)
		for (int j = 0; j < network.func_observ_depend[i].size(); j++)
			observ_func_affect[ network.func_observ_depend[i][j] -1 ].push_back(i + 1);

	/* functions -> parameters */
	func_param_affect = network.var_parameters; // functions by parameters (n x 1)

	/* parameters -> functions */
	param_func_affect = new vector<int>[network.rates->n_elt]; // parameters by functions
	//cout << "NUMBER OF RATES IN PARAM_FUNC_AFFECT: " << network.rates->n_elt << endl;
	for (int i = 0; i < network.func_param_depend.size(); i++)
		for (int j = 0; j < network.func_param_depend[i].size(); j++)
			param_func_affect[ network.func_param_depend[i][j] - 1 ].push_back(i + 1);

	/* parameters -> rxns */
	param_rxn_affect = new vector<int>[network.rates->n_elt]; // parameters by reactions
	for (int i = 0; i < network.reactions->n_rxn; i++)
		for (int j = 0; j < network.reactions->rxn[i]->n_rateLaw_params; j++)
			param_rxn_affect[ network.reactions->rxn[i]->rateLaw_indices[j] - 1 ].push_back(i + 1);

	/* rxns -> rxns */
	rxn_rxn_affect = new vector<int>[rxns.size()];
	vector<int> * species_rxn_affect = new vector<int>[x.size()];
	for (int i = 0; i < rxns.size(); i++) {
		// for given rxn, find all the species is affects
		// through its stoichiometry vector (not r_stoich, p_stoich)
		// push that reaction into appropriate species
		for (int j = 0; j < network.reactions->rxn[i]->n_reactants; j++)
			species_rxn_affect[ network.reactions->rxn[i]->r_index[j] -1 ].push_back(i);
		for (int j = 0; j < network.reactions->rxn[i]->n_products; j++)
			species_rxn_affect[ network.reactions->rxn[i]->p_index[j] -1 ].push_back(i);
	}
	for (int i = 0; i < rxns.size(); i++) {
		for (int j = 0; j < network.reactions->rxn[i]->n_reactants; j++)
			for (int k = 0; k < species_rxn_affect[ network.reactions->rxn[i]->r_index[j] -1 ].size(); k++)
				rxn_rxn_affect[i].push_back( species_rxn_affect[ network.reactions->rxn[i]->r_index[j] -1 ][k] );
		for (int j = 0; j < network.reactions->rxn[i]->n_products; j++)
			for (int k = 0; k < species_rxn_affect[ network.reactions->rxn[i]->p_index[j] -1 ].size(); k++)
				rxn_rxn_affect[i].push_back( species_rxn_affect[ network.reactions->rxn[i]->p_index[j] -1 ][k] );
	}
	for (int i = 0; i < rxns.size(); i++)
		remove_redundancies( rxn_rxn_affect[i] );
/*
	cout << "TESTING RXN_RXN_AFFECT: " << endl;
	for (int i = 0; i < rxns.size(); i++) {
		cout << "rxn " << i+1 << ": ";
		for (int j = 0; j < rxn_rxn_affect[i].size(); j++)
			cout << rxn_rxn_affect[i][j] << " ";
		cout << endl;
	}
	cout << endl;
*/
}

// type is either rxns(0) or rules(1) 
vector<pair<int, int> > pla::choose_rxns(double tau, int type){
	vector<pair<int, int> > result;
	pair<int, int> temp_pair;
	// initialize random number
	if (type == RXN_BASED) {
		ES_rxn_just_fired = -1;
		for (int i = 0; i < rxns.size(); i++) {
			if (rxns[i].type == ES) {
				if (tau == rxns[i].tau){
					if ( ES_rxn_just_fired != -1 ) {
						cout << "WARNING: attempted to fire 2 exact stochastic reactions (" << ES_rxn_just_fired << " and " << i
							 << ") in one step" << endl;
						//cout << ES_rxn_just_fired << " " << rxns[ES_rxn_just_fired].tau << ", " << i << " " << rxns[i].tau << endl;
						exit(1);
					}
					ES_rxn_just_fired = i;
				}
				else{
					temp_pair.second = 0;
					if (rxns[i].tau != numeric_limits<double>::infinity())
						rxns[i].old_const = rxns[i].propensity * (rxns[i].tau - curr_tau);
				}
			}
			else {
				temp_pair.first = i;
				// poisson with mean a_mu * tau
				// K_mu(tau) = Poisson()
				if (rxns[i].type == POISSON)
					temp_pair.second = double(gsl_ran_poisson( r, rxns[i].propensity * tau ));
     
				// K_mu(tau) = a_mu*tau + (a_mu*tau)^(1/2) * N(0,1)
				else if (rxns[i].type == LANGEVIN)
					temp_pair.second = double(rint(rxns[i].propensity * tau
							+ sqrt( rxns[i].propensity * tau ) * gsl_ran_gaussian( r, 1 )));

				else if (rxns[i].type == DETERMINISTIC)
					temp_pair.second = rint(rxns[i].propensity * tau);
    
				else
					cout << "WARNING: TRYING TO FIRE UNCLASSIFIED REACTION" << endl;
				if (temp_pair.second > 0)
					result.push_back(temp_pair);
			} // end else
		} // end for
	} // end if
/*
	else {
		for (int i = 0; i < rules.size(); i++) {
			temp_pair.first = i;

			if (rules[i].type == ES)
				if (tau == rules[i].tau){
					temp_pair.second = 1;
					rules[i].tau = numeric_limits<double>::infinity();
					rules[i].old_const = numeric_limits<double>::infinity();
				}
				else{
					temp_pair.second = 0;
					if (rules[i].tau != numeric_limits<double>::infinity())
						rules[i].old_const = rules[i].propensity * (rules[i].tau - curr_tau);
				}

			// poisson with mean a_mu * tau
			// K_mu(tau) = Poisson()
			else if (rules[i].type == POISSON) {
				temp_pair.second = double(gsl_ran_poisson( r, rules[i].propensity * tau ));
			}

			// K_mu(tau) = a_mu*tau + (a_mu*tau)^(1/2) * N(0,1)
			else if (rules[i].type == LANGEVIN)
				temp_pair.second = double(rint(rules[i].propensity * tau + sqrt( rules[i].propensity * tau ) * gsl_ran_gaussian( r, 1 )));
    
			else if (rules[i].type == DETERMINISTIC)
				temp_pair.second = rint(rules[i].propensity * tau);
    
			else
				cout << "WARNING: TRYING TO FIRE UNCLASSIFIED REACTION" << endl;

			result.push_back(temp_pair);
		}

	}
*/
	return result;
}
/*
bool pla::all_ES(int type, vector<int>) {
	int n = 0;
	if (type == RXN_BASED)
		for (int i = 0; i < rxns.size(); i++)
			if (!rxns[i].type == ES)
				return false;
			else
				for (int i = 0; i < rules.size(); i++)
					if (!rules[i].type == ES)
						return false;
	return true;
}
*/
double pla::calc_min_tau_ES () {
	//cout << "calculating min tau ES" << endl;
	double result = numeric_limits<double>::infinity();
	for (int i = 0; i < ES_rxns.size(); i++){
		//cout << ES_rxns[i] << "|" << rxns[ES_rxns[i]].tau << " ";
		if (rxns[ES_rxns[i]].type == ES && rxns[ES_rxns[i]].tau < result)
			result = rxns[ES_rxns[i]].tau;
	}
	//cout << endl;
/*
	else
		for (int i = 0; i < rules.size(); i++)
			if (rules[i].type == ES && rules[i].tau < result)
				result = rules[i].tau;
*/
	return result;
}

void pla::initialize_rule_stoich() {
	double sum;
	double * temp;
	for (int i = 0; i < rules.size(); i++) {
		for (int j = 0; j < x.size(); j++)
			rules[i].r_stoich[j] = 0;

		// init r_stoich
		// stoichiometry of reactants is based on their relative contribution to the groups that fire
		for (int j = 0; j < rules[i].r_indices.size(); j++) {
			sum = 0;
			temp = new double[ network.spec_groups_vec[ rules[i].r_indices[j] - 1 ]->n_elt ];
			for (int k = 0; k < network.spec_groups_vec[ rules[i].r_indices[j] - 1 ]->n_elt; k++)
				temp[k] = 0;
			for (int k = 0; k < network.spec_groups_vec[ rules[i].r_indices[j] - 1 ]->n_elt; k++) {
				temp[k] += x[ network.spec_groups_vec[ rules[i].r_indices[j] - 1 ]->elt_index[k] - 1 ];
							// * network.spec_groups_vec[ rules[i].r_indices[j] - 1 ]->elt_factor[k];
				sum += temp[k];
			}
			if (abs(sum) > 0)
				for (int k = 0; k < network.spec_groups_vec[ rules[i].r_indices[j] - 1 ]->n_elt; k++) {
					temp[k] /= sum;
					rules[i].r_stoich[ network.spec_groups_vec[ rules[i].r_indices[j] - 1 ]->elt_index[k] - 1 ] += temp[k];
				}
		}

		// init p stoich
		for (int j = 0; j < rules[i].p_indices.size(); j++) {
/*
			sum = 0;
			temp = new double[ network.spec_groups_vec[ rules[i].p_indices[j] - 1 ]->n_elt ];
			for (int k = 0; k < network.spec_groups_vec[ rules[i].p_indices[j] - 1 ]->n_elt; k++)
				temp[k] = 0;
			for (int k = 0; k < network.spec_groups_vec[ rules[i].p_indices[j] - 1 ]->n_elt; k++) {
				temp[k] += x[ network.spec_groups_vec[ rules[i].p_indices[j] - 1 ]->elt_index[k] - 1 ];
							// * network.spec_groups_vec[ rules[i].p_indices[j] - 1 ]->elt_factor[k];
				sum += temp[k];
			}
			if (abs(sum) > 0)
				for (int k = 0; k < network.spec_groups_vec[ rules[i].p_indices[j] - 1 ]->n_elt; k++) {
					temp[k] /= sum;
					rules[i].stoich[ network.spec_groups_vec[ rules[i].p_indices[j] - 1 ]->elt_index[k] - 1 ] += temp[k];
				}
*/
		}
	}
}
/*
void pla::simulate_rules(double *sample_times) {

	srand48(time(0));
	stop_time = sample_times[n_sample-1];
	int next_probe_index = 1;
	//cout << "writing out at time " << sys_time << endl;

	TL_out << left << setw(10) << "time";
	for (int i = 0; i < network.n_groups; i++)
		TL_out << left << setw(10) << string(network.spec_groups_vec[i]->name);
	TL_out << endl;
	TL_out << left << setw(10) << sys_time;
	for (int i = 0; i < network.n_groups; i++)
		TL_out << left << setw(10) << network.spec_groups_vec[i]->total_val;
	TL_out << endl;
  
	//sim_type = tau_comp_type;
	double min_tau_ES;
	// (1,2) initial value of tau (curr_tau) has been computed with either PRELEAP_PROPENSITIES or PRELEAP_SPECIES
	// outer loop: while total propensity > 0 && current time is less than stop time
	while (a_tot > 0 && sys_time < stop_time ) {
		while (1) {
			// (3) classify reactions (and update smallest tau of ES min_tau_ES)
			classify_rxns(curr_tau, RULE_BASED);

			// (4) calculate tau_mu for all ES rules using
			// equation 3 (draw new random number from poisson)
			// or 4 (rescale drawn random number)
			for (int i = 0; i < rules.size(); i++)
				if (rules[i].type == ES) {
					if (rules[i].tau == numeric_limits<double>::infinity()
							|| rules[i].old_const == numeric_limits<double>::infinity())
						rules[i].tau = abs(-log(drand48())/rules[i].propensity); // draw new rand num from poisson
					else
						rules[i].tau = abs(rules[i].old_const / rules[i].propensity); // rescale old rand num
				}

			// find mininum tau_mu for ES reactions
			min_tau_ES = calc_min_tau_ES(RULE_BASED, ES_rxns);

			// (5) (if all rules are ES) && (if tau is smaller than smallest tau-ES) -> tau = min(tau-ES_j)
			// later, consider how many rules (instead of all rules) need to be ES to do this
			if (all_ES(RULE_BASED) && curr_tau < min_tau_ES) {
				curr_tau = min_tau_ES;
			}

			// (6) if tau is bigger than min_tau_ES), set tau = min_tau_ES) and reclassify rules
			else if (curr_tau > min_tau_ES) {
				curr_tau = min_tau_ES;
				continue;
			}

			// (7) if you've made it this far, you've decided on a tau (congratulations!)
			// now, choose rules to fire
			vector<pair<int, int> > rules_to_fire = choose_rxns(curr_tau, RULE_BASED);

			// (9) fire rules: advance time by tau and update concentrations and concentrations
			vector<int> x_changed; // indices of species that changed during update concentrations

			// update stoichiometry vector of rule; should do this ONLY ONCE PER SIMULATION
			initialize_rule_stoich();
			for (int i = 0; i < rules_to_fire.size(); i++)
				update_concentrations_TL( rules_to_fire[i].first, rules_to_fire[i].second, x_changed, RULE_BASED);
			remove_redundancies(x_changed);
			update_x_g(FAUX);

			for (int i = 0; i < rules.size(); i++)
				pre_update_a[i] = rules[i].propensity;

			update_propensities_rules(x_changed);

			//postLeap_fix( rules_to_fire, curr_tau, RULE_BASED );
			break;
		}

		// compute new Tau (at end of loop to avoid calculating initial tau redundantly)
		// if it's a preleap procedure
		// calculate tau (avoid doing it twice initially)
		// if it's a postleap procedure
		// compute tau based on previously used tau (or just use initial, preleap computed tau if its the first run)
		if ( (sys_time - curr_tau < sample_times[next_probe_index]) && (sys_time >= sample_times[next_probe_index]) ) {

			next_probe_index++;
			cout << "writing out at time " << sys_time << endl;
			TL_out << left << setw(10) << sys_time;
			for (int i = 0; i < network.n_groups; i++)
				TL_out << left << setw(10) << network.spec_groups_vec[i]->total_val;
			TL_out << endl;

			TL_out_cdat << left << setw(10) << sys_time;
			for (int i = 0; i < x.size(); i++)
				TL_out_cdat << left << setw(10) << x[i];
			TL_out_cdat << endl;

		}
		if (a_tot > 0 || sys_time < stop_time) {
			curr_tau = compute_tau(POSTLEAP, curr_tau);
		}
    
	}
}
*/
ofstream _out_("/Users/ilyakorsunsky/Dropbox/CMU_stuff/BNG-trunk/Models2/PLA_test/fceri_ji_red_TEST.dat");

void print(vector<pair<int, int> > a, vector<rxn> rxns){
	cout << "RXNS TO FIRE" << endl;
	if (a.empty()) cout << "no rxns to fire" << endl;
	for (int i = 0; i < a.size(); i++)
		if (a[i].second != 0)
			cout << "\t" << a[i].first + 1 << " " << a[i].second << " type " << rxns[a[i].first].type << " tau "
				 << rxns[a[i].first].tau << endl;
	//cout << endl;
}

void pla::print_pops(){
	_out_ << "POPULATIONS" << endl;
	for (int i = 0; i < x.size(); i++){
		if (x[i] != x_offset[i+1]) {
			_out_ << "ERROR: for species #" << i+1 << ", x does not match x_offset" << endl;
			exit(1);
		}
		if (x[i] > 0)
			_out_ << "\t" << i+1 << " " << x[i] << endl;
	}
	_out_ << endl;
}

// Assume number is of the form xxx.yyy or xxx or 0.yyy 
// precision is the number of digits after decimal point in output
string convert(string dec, int precision) {
	//cout << "string init: " << dec << endl;
	string result, before, after;
	int i;

	// read all the digits before period
	for (i = 0; i < dec.size(); i++) {
		if (dec[i] > 57) {
			cout << "ERROR: trying to convert non decimal " << endl;
			exit(1);
		}
		if (dec[i] == '.') {
			break;
		}
		before += dec[i];
	}

	// read all the digits after period
	for (i = i+1; i < dec.size(); i++) {
		if (dec[i] > 57) {
			cout << "ERROR: trying to convert non decimal" << endl;
			exit(1);
		}
		after += dec[i];
	}
	//cout << "before: " << before << endl;
	//cout << "after: " << after << endl;

	// CASES:
	// there are "no digits" before period (0.yyy)
	if (before.size() == 1 && before[0] == '0') {
		int k;
		for (k = 0; k < after.size(); k++)
			if (after[k] != '0')
				break;

		// value = 0
		if (k == after.size()) {
			result = "0.";
			for (int i = 0; i < precision; i++)
				result += "0";
			result += "e+00";
		}
		else {
			result = after[k];
			result += ".";
			for (int j = k+1; j < after.size(); j++) {
				result += after[j];
				if (result.size() > precision + 1)
					break;
			}
			while (result.size() < precision + 2)
				result += '0';
			result += "e-";
			if (k < precision + 2)
				result += '0';
			stringstream buf1;
			buf1 << k + 1;
			result.insert(result.size(), buf1.str());
		}
	}
	// there is no period (same as there are no digits after period) (xxx)
	else if (after.empty()) {
		result = before[0];
		result += '.';
		for (int k = 1; k < before.size(); k++) {
			result += before[k];
			if (result.size() > precision + 1)
				break;
		}
		while (result.size() < precision + 2)
			result += '0';
		result += "e+";
		if (before.size() - 1 < precision + 2)
			result += '0';
		stringstream buf;
		buf << before.size() - 1;
		result.insert(result.size(), buf.str());
	}
	// there are non-zero digits both before and after period (xxx.yyy)
	else {
		result = before[0];
		result += ".";
		for (int k = 1; k < before.size(); k++) {
			if (result.size() > precision + 1)
				break;
			stringstream buf;
			buf << before[k];
			result.insert(result.size(), buf.str());
		}
		for (int k = 0; k < after.size(); k++) {
			if (result.size() > precision + 1)
				break;
			stringstream buf;
			buf << after[k];
			result.insert(result.size(), buf.str());
		}
		while (result.size() < precision + 2)
			result += '0';
		result += "e+";
		if (before.size() - 1 < precision + 2)
			result += "0";
		stringstream buf;
		buf << before.size() - 1;
		result.insert(result.size(), buf.str());
	}

	return result;
}

void pla::check_all_ES(vector<pair<int, int> > vec) {
	for (int i = 0; i < vec.size(); i++)
		if ( rxns[vec[i].first].type != ES  && vec[i].second > 0) {
			cout << "ERROR: rxn # " << vec[i].first + 1 << " is not ES and fired " << vec[i].second << " times" << endl;
			exit(1);
		}
}

// main tau leaping method, takes Network as argument 
void pla::simulate(int tau_comp_type, double *sample_times) {

	int num_rxns_fired;

	srand48(time(0));
	stop_time = sample_times[n_sample-1];
	int next_probe_index = 1;

	// check to see if INITIAL tau will pass the next output time
	if (curr_tau + sys_time > sample_times[next_probe_index])
		curr_tau = sample_times[next_probe_index] - sys_time;

	TL_out_cdat << "#";
	TL_out_cdat << right << setw(22) << "time";
	for (int i = 0; i < x.size(); i++)
		TL_out_cdat << right << setw(24) << i+1;
	TL_out_cdat << endl;

	buf.str("");
	buf << sys_time;
	TL_out_cdat << " " << convert(buf.str(), 16) << "  ";
	for (int i = 0; i < x.size(); i++) {
		buf.str("");
		buf << x[i];
		TL_out_cdat << convert(buf.str(), 16) << "  ";
	}
	TL_out_cdat << endl;

	TL_out << "#";
	TL_out << right << setw(14) << "time";
	//cout << "#";
	//cout << right << setw(14) << "time";

	for (int i = 0; i < network.n_groups; i++) {
		TL_out << right << setw(16) << string(network.spec_groups_vec[i]->name);
		//cout << right << setw(16) << string(network.spec_groups_vec[i]->name);
	}
	//cout << endl;
	TL_out << endl;

	buf.str("");
	buf << sys_time;
	TL_out << " " << convert(buf.str(), 8) << "  ";
	//cout << " " << convert(buf.str(), 8) << "  ";
	for (int i = 0; i < network.n_groups; i++) {
		buf.str("");
		buf << network.spec_groups_vec[i]->total_val;
		TL_out << convert(buf.str(), 8) << "  ";
		//cout << convert(buf.str(), 8) << "  ";
	}
	TL_out << endl;
	//cout << endl;
/*
	a_out << left << setw(10) << "time";
	for (int i = 0; i < rxns.size(); i++)
		a_out << left << setw(10) << i+1;
	a_out << endl;
	a_out << left << setw(10) << sys_time;
	for (int i = 0; i < rxns.size(); i++)
		a_out << left << setw(10) << rxns[i].propensity;
	a_out << endl;
*/
	cout << left << setw(10) << "time" << left << setw(20) << "CPU seconds" << left << setw(10) << "n_steps" << left << setw(10)
		 << "n_rxns" << left << setw(10) << "acceptance ratio" << endl;

	sim_type = tau_comp_type;
	double min_tau_ES;
	long double init_cpu_time = clock();
	num_rxns_fired_tot = 0;

	// classify all rxns initially
	// in method, initialize ES_rxns and active_rxns vectors
	ES_rxns = classify_rxns(curr_tau);

	// (1,2) initial value of tau (curr_tau) has been computed with either PRELEAP_PROPENSITIES or PRELEAP_SPECIES
	// outer loop: while total propensity > 0 && current time is less than stop time
	while (a_tot > 0.0 && sys_time < stop_time ) {
		num_steps++;
		while (1) {

			// (3) reclassify reactions (and update smallest tau of ES min_tau_ES)
			// NOTE: ES_rxns includes indices of rxns that don't have both a = 0 and tau = inf and are ES
			ES_rxns = reclassify_rxns(curr_tau);
/*
			cout << "ES rxns ";
			for (int i = 0; i < ES_rxns.size(); i++)
				cout << ES_rxns[i] << " ";
			//cout << endl;
			cout << "active rxns ";
			for (list<int>::iterator it = active_rxns.begin(); it != active_rxns.end(); it++)
				cout << *it << " ";
			cout << endl;

			static int count1 = 0;
			count1++;
			if (count1 > 20)
				exit(1);
*/
/*
			//cout << "classified reactions " << endl;
			cout << "all ES taus BEFORE: ";
			for (int i = 0; i < ES_rxns.size(); i++) {
				//cout << ES_rxns[i] << "|" << rxns[ES_rxns[i]].tau << " tau " << rxns[ES_rxns[i]].tau << " ";
				cout << ES_rxns[i] << "|" << rxns[ES_rxns[i]].tau << " a " << rxns[ES_rxns[i]].propensity << " old_c "
					 << rxns[ES_rxns[i]].old_const << " ";
			}
			cout << endl;

			cout << "all taus: ";
			for (int i = 0; i < rxns.size(); i++)
				if (rxns[i].tau != numeric_limits<double>::infinity())
					cout << i+1 << "|tau " << rxns[i].tau << " a " << rxns[i].propensity << " old_c " << rxns[i].old_const << " ";
			cout << endl;
*/
			// (4) calculate tau_mu for all ES rxns using equation 3 (draw new random number from poisson)
			// or 4 (rescale drawn random number)
			for (int j = 0; j < ES_rxns.size(); j++) {
				int i = ES_rxns[j];
				if (rxns[i].tau == numeric_limits<double>::infinity() || rxns[i].old_const == numeric_limits<double>::infinity())
					rxns[i].tau = abs(-log(drand48())/rxns[i].propensity); // draw new rand num from poisson

				// NOTE: THIS ABS() IS KIND OF A HACK. SOMETIMES ARGUMENT RETURNS -INF AND I DON'T KNOW WHY
				// UPDATE: SHOULD NEVER HAPPEN NOW THAT WE DON'T CONSIDER ES RXNS WITH PROP == 0
				else
					rxns[i].tau = abs(rxns[i].old_const / rxns[i].propensity); // rescale old rand num
			}
/*
			cout << "all taus: ";
			for (int i = 0; i < rxns.size(); i++)
				if (rxns[i].tau != numeric_limits<double>::infinity())
					cout << i+1 << "|tau " << rxns[i].tau << " a " << rxns[i].propensity << " old_c " << rxns[i].old_const << " ";
			cout << endl;

			cout << "all ES taus AFTER:  ";
			for (int i = 0; i < ES_rxns.size(); i++) {
				//cout << ES_rxns[i] << "|" << rxns[ES_rxns[i]].tau << " tau " << rxns[ES_rxns[i]].tau << " ";
				cout << ES_rxns[i] << "|" << rxns[ES_rxns[i]].tau << " a " << rxns[ES_rxns[i]].propensity << " old_c "
					 << rxns[ES_rxns[i]].old_const << " ";
			}
			cout << endl << endl;
*/
			//cout << "got intial tau guess" << endl;

			// find mininum tau_mu for ES reactions
			min_tau_ES = calc_min_tau_ES();

			// (5) (if all rxns are ES) && (if tau is smaller than smallest tau-ES) -> tau = min(tau-ES_j)
			// later, consider how many rxns (instead of all rxns) need to be ES to do this
			//bool all_ES = false;
			if (ES_rxns.size() == rxns.size() - num_zero_rxns) { // && curr_tau < min_tau_ES) {
				curr_tau = min_tau_ES;
				//all_ES = true;
			}

			// (6) if tau is bigger than min_tau_ES), set tau = min_tau_ES) and reclassify rxns
			else if (curr_tau > min_tau_ES) {
				curr_tau = min_tau_ES;
				continue;
			}
/*
			if (!all_ES) {
				cout << "ERROR: some rxns are not ES in forced all_ES simulation " << endl;
				for (int i = 0; i < rxns.size(); i++)
					if (rxns[i].type != 0)
						cout << i+1 << "|" << rxns[i].type << " ";
				cout << endl;
				cout << "num of nonzero ES rxns " << ES_rxns.size() << endl;
				cout << "num of zero rate rxns " << num_zero_rxns << endl;
				cout << "total num of rxns " << rxns.size() << endl;
				exit(1);
			}
*/
			// check to see if NEXT tau will pass the next output time
			if (curr_tau + sys_time > sample_times[next_probe_index])
				curr_tau = sample_times[next_probe_index] - sys_time;
/*
			cout << "all ES taus RIGHT BEFORE CHOOSE: ";
			for (int i = 0; i < ES_rxns.size(); i++)
				cout << ES_rxns[i] + 1 << "|" << rxns[ES_rxns[i]].tau << " a " << rxns[ES_rxns[i]].propensity << " old_c "
					 << rxns[ES_rxns[i]].old_const << " ";
			cout << endl;

			cout << "all taus: ";
			for (int i = 0; i < rxns.size(); i++)
				if (rxns[i].tau != numeric_limits<double>::infinity())
					cout << i+1 << "|tau " << rxns[i].tau << " a " << rxns[i].propensity << " old_c " << rxns[i].old_const << " ";
			cout << endl;
*/
			// (7) if you've made it this far (and you're doing a preleap simulation), you've decided on a tau (congratulations!)
			// now, choose rxns to fire
			// rxns_to_fire only contains rxns who have at least 1 firing this round
			vector<pair<int, int> > rxns_to_fire = choose_rxns(curr_tau, RXN_BASED);
			num_rxns_fired = 0;

			//print(rxns_to_fire, rxns);
			//cout << "decided on tau " << curr_tau << endl;
			//cout << "number of rxns " << network.reactions->n_rxn << endl;
			//print(rxns_to_fire);
			//print_pops();

			// save old population values (to be used in the back up)
			for (int i = 0; i < x.size(); i++) {
				x_old[i] = x[i];
				x_offset_old[i+1] = x_offset[i+1];
			}

			// fire rxns: advance time by tau and update concentrations
			// if ES reaction fires, store its old a*(tau_mu-tau) and set tau to infinity
			// FOR LATER: don't need to check for []<0 if postleap procedure
			//vector<int> x_changed;
			for (int i = 0; i < rxns_to_fire.size(); i++) {
				num_rxns_fired += rxns_to_fire[i].second;
				//update_concentrations_TL( rxns_to_fire[i].first, rxns_to_fire[i].second, x_changed, RXN_BASED);
				update_concentrations_TL( rxns_to_fire[i].first, rxns_to_fire[i].second, RXN_BASED);
			}
/*
			// TESTING FOR NEGATIVE POPULATIONS AND MULTIPLE CONCURRENT ES FIRINGS
			bool stop = false;
			for (int i = 0; i < x.size(); i++) {
				if (x[i] != 0) {
					cout << i+1 << "|" << x[i] << " ";
				}
				if (x[i] < 0) stop = true;
			}
			cout << endl;
			cout << "all ES? " << boolalpha << all_ES << endl;
			print(rxns_to_fire, rxns);

			//cout << " min tau ES " << min_tau_ES << endl;
			if (stop) {
				//print(rxns_to_fire, rxns);
				exit(1);
			}
*/
			//print_pops();
			//for (int i = 0; i < 100; i++)
				//_out_ << "-";
			//_out_ << endl;

			for (int i = 0; i < rxns.size(); i++)
				pre_update_a[i] = rxns[i].propensity;
	
			update_propensities( rxns_to_fire );

			//cout << "preliminary tau " << curr_tau << " min tau ES " << min_tau_ES << endl;
			//print(rxns_to_fire, rxns);
			//vector<int> ES_rxns = classify_rxns(tau);

			postLeap_fix( rxns_to_fire, curr_tau, RXN_BASED, num_rxns_fired );
			//postLeap_fix( rxns_to_fire, curr_tau, RXN_BASED, num_rxns_fired, all_ES );
			break;
		}

		// Write out data if it is time to
		//cout << "atot: " << a_tot << ", time: " << sys_time << endl;
		//cout << "\tnext write out time: " << sample_times[next_probe_index] << endl;
		//cout << "\t number of rxns fired for this tau: " << num_rxns_fired << endl;
		//cout << "sys_time : " <<  sys_time << " curr tau : " << curr_tau <<  " sys_time - curr tau: " << sys_time - curr_tau
			 //<< " sample time: " << sample_times[next_probe_index] << endl;
		if ( (sys_time - curr_tau < sample_times[next_probe_index]) && (sys_time >= sample_times[next_probe_index]) ) {

			buf.str("");
			buf << sys_time;
			TL_out << " " << convert(buf.str(), 8) << "  ";
			//cout << " " << convert(buf.str(), 8) << "  ";
			for (int i = 0; i < network.n_groups; i++) {
				buf.str("");
				buf << network.spec_groups_vec[i]->total_val;
				TL_out << convert(buf.str(), 8) << "  ";
				//cout << convert(buf.str(), 8) << "  ";
			}
			TL_out << endl;
			//cout << endl;
/*
			for (int i = 0; i < network.reactions->n_rxn; i++) {
				PLA_out << left << setw(10) << rxn_fire_count[i];
				rxn_fire_count[i] = 0;
			}
			PLA_out << endl;
*/
			cout << left << setw(10) << sys_time << left << setw(20) << ((double)clock() - init_cpu_time) / (double)CLOCKS_PER_SEC
				 << left << setw(10) << num_steps << left << setw(10) << num_rxns_fired_tot << left << setw(10)
				 << (double)num_accepted / (double)(num_accepted + num_rejected) << endl;
			num_steps = num_rxns_fired_tot = num_rejected = num_accepted = 0;

			buf.str("");
			buf << sys_time;
			TL_out_cdat << " " << convert(buf.str(), 16) << "  ";
			for (int i = 0; i < x.size(); i++) {
				buf.str("");
				buf << x[i];
				TL_out_cdat << convert(buf.str(), 16) << "  ";
			}
			TL_out_cdat << endl;
/*
			a_out << ":" << left << setw(10) << sys_time;
			for (int i = 0; i < rxns.size(); i++)
				a_out << left << setw(10) << rxns[i].propensity;
			a_out << endl;
*/
			init_cpu_time = clock();

			if (++next_probe_index >= n_sample) {
				//cout << "num rejected: " << num_rejected << endl;
				//cout << "num accepted: " << num_accepted << endl;
				//cout << "total ratio of acceptance: " << (double)num_accepted / (double)(num_accepted + num_rejected) << endl;
				return;
			}
		}

		// compute new Tau (at end of loop to avoid calculating initial tau redundantly)
		// if it's a preleap procedure
		// calculate tau (avoid doing it twice initially)
		// if it's a postleap procedure
		// compute tau based on previously used tau (or just use initial, preleap computed tau if its the first run)
		if (a_tot > 0 || sys_time <= stop_time) {
			curr_tau = compute_tau(tau_comp_type, curr_tau);
			//cout << "computed tau: " << curr_tau << endl;
		}
	}
}

void print(double * arr, int size) {
	for (int i = 0; i < size; i++)
		if (arr[i] != 0)
			cout << i+1 << "|" << arr[i] << " ";
	cout << endl;
}

// returns TRUE if a propensity has changed by more than epsilon 
// by reference, this returns the "reason" for the failure; that is, the epsilon or beta/a_orig 
bool pla::check_propensities(double eps) { 

	// check propensity of ES rxn (if one fired)
	// CASE 1: propensity has not changed by more than epsilon => OK, check the rest of the leap
	// CASE 2: propensity has increased by more than epsilon   => rxn fires sometime in the middle of leap, don't fire and need to back up
	// CASE 3: propensity has decreased by more than epsilon   => rxn fires sometime after leap, don't fire but leap is OK
	if (ES_rxn_just_fired > -1) {
		if (abs(delta_a[ES_rxn_just_fired]) > abs(eps * (rxns[ES_rxn_just_fired].propensity - delta_a[ES_rxn_just_fired]))) {
			cout << "delta a " << abs(delta_a[ES_rxn_just_fired]) << " | e*a "
				 << abs(eps * (rxns[ES_rxn_just_fired].propensity - delta_a[ES_rxn_just_fired])) << endl;
			ES_rxn_just_fired = -1;
			if (delta_a[ES_rxn_just_fired] > 0) {
				cout << "ES rxn propensity changed by too much" << endl;
				return true;
			}
		}
	}

	// check the effects of the leap on all other reactions
	// CASE 1: passed the leap condition test                 => no problem
	// CASE 2: failed but no reactant changed by more than 1  => no problem
	// CASE 3: failed and some reactant change by more than 1 => problem
	int k;
	for (int i = 0; i < _props_to_check.size(); i++) {
		if (abs(delta_a[_props_to_check[i]]) > abs(eps * (rxns[_props_to_check[i]].propensity - delta_a[_props_to_check[i]]))) {
			for (int j = 0; j < network.reactions->rxn[_props_to_check[i]]->n_reactants; j++) {
				k = network.reactions->rxn[_props_to_check[i]]->r_index[j] - 1; // 0 based reactant index
				if ( abs(x_old[k] - x[k]) > 1 ) return true;
			}
		}
	}
	return false;

/* OLD CODE: uses beta instead of explicitly checking whether any reactant changed by more than 1

	//cout << "nonzero delta_a " << endl;
	//print(delta_a, rxns.size());

	//cout << "beta: ";
	for (int i = 0; i < _props_to_check.size(); i++) {
		//for ES reactions, they can change
		//for Poisson reactions, set threshold for da to max(eps*a_orig, beta)
		//if (rxns[_props_to_check[i]].type == POISSON || rxns[_props_to_check[i]].type == ES) {
		double derivs[network.reactions->rxn[_props_to_check[i]]->n_reactants];
		for (int j = 0; j < network.reactions->rxn[_props_to_check[i]]->n_reactants; j++)
			derivs[j] = deriv(i, network.reactions->rxn[_props_to_check[i]]->r_index[j] - 1);
		double beta = numeric_limits<double>::infinity();
		//double beta = 0;
		for (int j = 0; j < network.reactions->rxn[_props_to_check[i]]->n_reactants; j++) {
			if (abs(derivs[j]) < beta) {
				//if (derivs[j] > 0 && derivs[j] < beta)
				beta = abs(derivs[j]);
			}
		}

		//cout << _props_to_check[i] + 1 << "|" << " abs change " << abs(delta_a[_props_to_check[i]]) << " beta " << beta
			 //<<  " max E change " << abs(eps * (rxns[_props_to_check[i]].propensity - delta_a[_props_to_check[i]]))
			 //<< " compared against " << max(abs(eps * (rxns[_props_to_check[i]].propensity - delta_a[_props_to_check[i]])), beta )
			 //<< endl;

		if (abs(delta_a[_props_to_check[i]]) > max(abs(eps * (rxns[_props_to_check[i]].propensity - delta_a[_props_to_check[i]])), beta )) {
			//cout << "rejected by rxn " << _props_to_check[i] + 1 << " of type " << rxns[_props_to_check[i]].type << " and propensity " << rxns[_props_to_check[i]].propensity << " and beta " << beta << " and change " << abs(delta_a[_props_to_check[i]]) << endl;
			for (int j = 0; j < network.reactions->rxn[_props_to_check[i]]->n_reactants; j++)
				//cout << network.reactions->rxn[_props_to_check[i]]->r_index[j]  << "|pop " << x[ network.reactions->rxn[_props_to_check[i]]->r_index[j] - 1 ] << " deriv " << abs(derivs[j]) << " ";
				//cout << network.reactions->rxn[_props_to_check[i]]->r_index[j]  << "|pop " << x[ network.reactions->rxn[_props_to_check[i]]->r_index[j] - 1 ] << " ";
				//cout << endl;

				return true;
		}
	//}

	// for Langevin and deterministic reactions, set threshold for da to eps*a_orig
		//else if (rxns[_props_to_check[i]].type == LANGEVIN || rxns[_props_to_check[i]].type == DETERMINISTIC) {
			//cout << _props_to_check[i] + 1 << "|" << " abs change " << abs(delta_a[_props_to_check[i]]) << " max E change " << abs(eps * (rxns[_props_to_check[i]].propensity - delta_a[_props_to_check[i]])) << endl;
			//if (abs(delta_a[_props_to_check[i]]) > abs(eps * (rxns[_props_to_check[i]].propensity - delta_a[_props_to_check[i]]))) {
				//cout << "rejected" << endl;
				//return true;
			//}
		//}
	}

	//static int exit_count = 0;
	//if (exit_count++ > 5) exit(1);

	//cout << "------------------------------------------------------------------------" << endl;
	//cout << endl;
	return false;
*/

/* OLD CODE: CHECKS PROPENSITY CHANGE IN ALL RXNS
	for (int i = 0; i < rxns.size(); i++) {
		// don't check ES reactions, b/c they can only fire once
		// for Poisson reactions, set threshold for da to max(eps*a_orig, beta)
		if (rxns[i].type == POISSON) {
			double derivs[network.reactions->rxn[i]->n_reactants];
			for (int j = 0; j < network.reactions->rxn[i]->n_reactants; j++)
				derivs[j] = deriv(i, network.reactions->rxn[i]->r_index[j] - 1);
			double beta = numeric_limits<double>::infinity();
			for (int j = 0; j < network.reactions->rxn[i]->n_reactants; j++)
				if (derivs[j] > 0 && derivs[j] < beta)
					beta = derivs[j];

			if (abs(delta_a[i]) > max(abs(eps * (rxns[i].propensity - delta_a[i])), beta )) {
				return true;
			}
		}

		// for Langevin and deterministic reactions, set threshold for da to eps*a_orig
		else if (rxns[i].type == LANGEVIN || rxns[i].type == DETERMINISTIC) {
			if (abs(delta_a[i]) > abs(eps * (rxns[i].propensity - delta_a[i]))) {
				return true;
			}
		}
	}
	return false;
*/
}

bool pla::check_for_neg_c() {
	for (int i = 0; i < x.size(); i++)
		if (x[i] < 0)
			return true;
	return false;
}

vector<pair<int, int> > pla::distribute_firings( vector<pair<int, int> > rules_fired ) { 
	vector<pair<int, int> > result;
	for (int i = 0; i < rxns.size(); i++) {
		pair<int, int> temp_pair;
		temp_pair.first = i;
		temp_pair.second = 0;
		result.push_back(temp_pair);
	}
	double sum;
	Rxn * irxn;
	double *r_prods;
	unsigned int *n;
	for (int i = 0; i < rules.size(); i++) {

		sum = 0;
		int num_rxns = rules[i].rxns_indices.size();
		r_prods = new double[num_rxns];
		for (int j = 0; j < num_rxns; j++) {
			r_prods[j] = 1;
			irxn = network.reactions->rxn[ rules[i].rxns_indices[j] - 1 ];
			if (irxn->rateLaw_type == ELEMENTARY && irxn->n_reactants == 2 && irxn->r_index[0] == irxn->r_index[1])
				r_prods[j] *= x[ irxn->r_index[0] - 1 ]*(x[ irxn->r_index[0] - 1 ]-1);
			else
				for (int k = 0; k < irxn->n_reactants; k++)
					r_prods[j] *= x[ irxn->r_index[k] - 1 ];
			sum += r_prods[j];
		}

		n = new unsigned int[ num_rxns ];
		for (int j = 0; j < num_rxns; j++)
			r_prods[j] /= sum;
        
		gsl_ran_multinomial (r, num_rxns, rules_fired[i].second, r_prods, n);

		for (int j = 0; j < num_rxns; j++)
			result[ rules[i].rxns_indices[j] - 1 ].second += n[j];
    
		delete [] n;
		delete [] r_prods;
	}
	return result;
}

pair<int, double> pla::find_min_tau(vector<pair<int, int> > rxns_fired) {
	pair<int, double> result;
	result.first = rxns_fired[0].first;
	result.second = rxns_fired[0].second;
	for (int i = 1; i < rxns_fired.size(); i++) {
		if ( rxns[rxns_fired[i].first].tau < result.second ) {
			result.first = rxns_fired[i].first;
			result.second = rxns[rxns_fired[i].first].tau;
		}
	}
	return result;
}

pair<int, double> pla::find_min_tau() {
	//cout << "WARNING: postleap check passed empty rxns_fired vector" << endl;
	//exit(1);
	pair<int, double> result;
	result.first = 0;
	result.second = rxns[0].tau;
	for (int i = 1; i < rxns.size(); i++) {
		if ( rxns[i].tau < result.second ) {
			result.first = i;
			result.second = rxns[i].tau;
		}
	}
	return result;
}

void pla::print_nonzero_propensities() {
	cout << "Propensities: ";
	for (int i = 0; i < rxns.size(); i++)
		if (rxns[i].propensity != 0)
			cout << i+1 << "|" << rxns[i].propensity << " ";
	cout << endl;

	cout << "Rxn types: ";
	for (int i = 0; i < rxns.size(); i++)
		if (rxns[i].propensity != 0)
			cout << i+1 << "|" << rxns[i].type << " ";
	cout << endl;
}

bool print_nonzero_populations(vector<double> pops) {
	bool stop = false;
	//cout << "nonzero species populations: ";
	for (int i = 0; i < pops.size(); i++) {
		if (pops[i] != 0)
			//cout << i+1 << "|" << pops[i] << " ";
			if (pops[i] < 0)
				stop = true;
	}
	//cout << endl;

	return stop;
	//if (stop) exit(1);
}

void pla::print_delta_a() {
	cout << "delta a: ";
	for (int i = 0; i < _props_to_check.size(); i++)
		if (delta_a[_props_to_check[i]] != 0)
			cout << _props_to_check[i] + 1 << "|" << delta_a[_props_to_check[i]] << " ";
	cout << endl;
}

// returns vector of reaction (indices) to fire and the new tau by reference 
// updates system time when final tau step is chosen 
// type = rxn(0) or rule(1) 
// precondition: parameter tau should never be less than or equal to min_tau_ES
//void pla::postLeap_fix(vector<pair<int, int> > &rxns_fired, double &tau, int type, int& num_rxns_fired, bool all_ES) {
void pla::postLeap_fix(vector<pair<int, int> > &rxns_fired, double &tau, int type, int& num_rxns_fired) {
	//////////////////////////
	// check if leap was OK //
	//////////////////////////
	if (sim_type == POSTLEAP) {
		// if propensities changed too much, continue with method
		if (check_propensities(epsilon)) { num_rejected++; cout << "rejected ";}// << counter << " " << tau << endl; exit(1); }
		// otherwise, commit the update and leave method
		else {
			/////////////////////
			// ACCEPT THE LEAP //
			/////////////////////
/*
			cout << "TIME " << sys_time << endl;
			print(rxns_fired, rxns);
			bool stop = print_nonzero_populations(x);
			print_nonzero_propensities();
			print_delta_a();
			if (stop)
				exit(1);
			cout << "----------------------------------------------------------------" << endl;
*/ 
			bool stop = print_nonzero_populations(x);
			if (stop) {
				cout << "ERROR: found nonzero population" << endl;
				exit(1);
			}

			//check_all_ES(rxns_fired);
			//cout << "accepted tau " << tau << endl;// << " min tau ES " << calc_min_tau_ES(ES_rxns) << endl;
			//print(rxns_fired, rxns);
			//vector<int> ES_rxns = classify_rxns(tau);

			// if accepted leap with ES rxn, fire it
			// and set its tau and const to infinity
			if (ES_rxn_just_fired > -1) {
				update_concentrations_TL(ES_rxn_just_fired, 1, RXN_BASED);
				update_propensities(ES_rxn_just_fired);

				rxns[ES_rxn_just_fired].tau = numeric_limits<double>::infinity();
				rxns[ES_rxn_just_fired].old_const = numeric_limits<double>::infinity();

				num_rxns_fired_tot++;
			}

			num_accepted++;
			sys_time += tau;
			num_rxns_fired = 0;
			for (int i = 0; i < rxns_fired.size(); i++) {
				rxn_fire_count[rxns_fired[i].first] += rxns_fired[i].second;
				num_rxns_fired += rxns_fired[i].second;
			}
			num_rxns_fired_tot += num_rxns_fired;
			return;
		}
	}

	// if PRELEAP, check if species are negative
	// if they are, do postleap fix recursively
	else {
		if (check_for_neg_c()); // if any concentrations are negative, continue with method
		else {
			/////////////////////
			// ACCEPT THE LEAP //
			/////////////////////

			// if accepted ES rxn firing, set its tau and const to infinity
			if (ES_rxn_just_fired > -1) {
				update_concentrations_TL(ES_rxn_just_fired, 1, RXN_BASED);
				update_propensities(ES_rxn_just_fired);

				rxns[ES_rxn_just_fired].tau = numeric_limits<double>::infinity();
				rxns[ES_rxn_just_fired].old_const = numeric_limits<double>::infinity();

				num_rxns_fired_tot++;
			}

			sys_time += tau;
			num_rxns_fired = 0;
			for (int i = 0; i < rxns_fired.size(); i++) {
				rxn_fire_count[rxns_fired[i].first] += rxns_fired[i].second;
				num_rxns_fired += rxns_fired[i].second;
			}
			num_rxns_fired_tot += num_rxns_fired;
			return;
		}
	}

	//////////////////////////
	// LEAP FAILED: BACK UP //
	//////////////////////////
	// save delta_a
	for (int i = 0; i < rxns.size(); i++) {
		delta_a_p[i] = delta_a[i];
	}

	// reverse all updates
	for (int i = 0; i < rxns_fired.size(); i++) {
		redo_update_concentrations(rxns_fired[i].first, rxns_fired[i].second);
	}
	update_propensities(rxns_fired);

	// decrease tau
	tau *= 0.5;

	// choose rxns from binomial distribution
	// parameters for B( rxns_fired[i].second, p ):
	// rxns_fired[i].second = number of reactions that happen in total interval
	// p = chance that reaction fires in first part of interval
	if (ES_rxn_just_fired > -1)
		num_rxns_in_postleap = 1;
	else
		num_rxns_in_postleap = 0;
	for (int i = 0; i < rxns_fired.size(); i++) {
		rxns_fired[i].second = gsl_ran_binomial (r, 0.5, rxns_fired[i].second);
		num_rxns_in_postleap += rxns_fired[i].second;
	}

	//cout << "after binomial rxns_fired. tau " << tau << endl;
	//print(rxns_fired);

	//cout << "after empty fix rxns_fired. tau " << tau << endl;
	//print(rxns_fired);

	//cout << "about to fire rxns" << endl;

	// fire rxns
	//vector<int> x_changed;
	for (int i = 0; i < rxns_fired.size(); i++) {
		//update_concentrations_TL( rxns_fired[i].first, rxns_fired[i].second, x_changed, RXN_BASED); // CHANGE THISSSSSSSSSSSSSSSSSSSSSS FOR RULE BASED!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		update_concentrations_TL( rxns_fired[i].first, rxns_fired[i].second, RXN_BASED); // CHANGE THISSSSSSSSSSSSSSSSSSSSSS FOR RULE BASED!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	}
	update_propensities(rxns_fired);

	postLeap_fix( rxns_fired, tau, type, num_rxns_fired ); // CHANGE THISSSSSSSSSSSSSSSSSSSSSSSSSS FOR RULE BASED!!!!!!!!!!!!!!!!!!!!!!!!!!!
}

// NOTE: PRELEAP PROPENSITIES IS NOT IMPLEMENTED COMPLETELY HERE 
double pla::compute_tau(int type, double old_tau) {

	if ( type == PRELEAP_PROPENSITIES) {
		double **derivs = new double*[rxns.size()];
		double **f = new double*[rxns.size()];
		for (int i = 0; i < rxns.size(); i++) {
			derivs[i] = new double[x.size()];
			f[i] = new double[rxns.size()];
		}
		for (int j = 0; j < rxns.size(); j++) {
			for (int jp = 0; jp < rxns.size(); jp++) {
				f[j][jp] = 0;
				for (int i = 0; i < x.size(); i++) {
					if (rxns[jp].stoich[i] != 0) {
						derivs[j][i] = deriv(j,i);
						f[j][jp] += rxns[jp].stoich[i] * derivs[j][i];
					}
				}
			}
		}
		for (int i = 0; i < rxns.size(); i++) {
			delete [] derivs[i];
			delete [] f[i];
		}
		delete [] derivs;
		delete [] f;
		//return result_tau;
	} // end preleap_propensities

	if (type == PRELEAP_SPECIES) {
		double e[ x.size() ];
		for (int i = 0; i < x.size(); i++)
			e[i] = max( epsilon*x[i]/g[i], 1.0);

		//cout << "e[i]: ";
		//for (int i = 0; i < x.size(); i++)
			//cout << e[i] << " ";
		//cout << endl;

		double mu[ x.size() ];
		for (int i = 0; i < x.size(); i++) {
			mu[i] = 0;
			for (int j = 0; j < rxns.size(); j++)
				mu[i] += rxns[j].stoich[i]*rxns[j].propensity;
		}

		double sig2[ x.size() ];
		for (int i = 0; i < x.size(); i++) {
			sig2[i] = 0;
			for (int j = 0; j < rxns.size(); j++)
				sig2[i] += pow( double(rxns[j].stoich[i]), 2)*rxns[j].propensity;
		}

		double result_tau = numeric_limits<double>::infinity();
		double tau[x.size()];
		for (int i = 0; i < x.size(); i++) {
			tau[i] = min( e[i] / abs( mu[i] ), pow(e[i], 2) / sig2[i] );
			if (tau[i] < result_tau)
				result_tau = tau[i];
		}
		return result_tau;
	} // end preleap_species

	// at this point, the old_tau was accepted
	if (type == POSTLEAP) {
/*
		double max_change = 0;
		double min_tau_ES = calc_min_tau_ES(RXN_BASED, ES_rxns);
		for (int i = 0; i < rxns.size(); i++)
			if ( max_change < abs(delta_a[i]/ (rxns[i].propensity - delta_a[i])))
				max_change =  abs(delta_a[i]/ (rxns[i].propensity - delta_a[i]));

		// old tau is too small but no rxns fired, so we don't know by how much to increase it
		// increase it by arbitrary factor
		if (max_change == 0) {
			return old_tau * 2;
		}

		// old tau is limited by the new min ES tau, so keep it
		// we repeat this step in simulation -> should we?
		else if (old_tau >= min_tau_ES) {
			return min_tau_ES;
		}

		// old tau is too small and we can rescale it based on how much the percent change felt short of epsilon
		else if (max_change < epsilon) {
			return old_tau * (epsilon/max_change);
		}

		// old_tau was limited by beta, so keep it
		else {
			return old_tau;
		}
*/
 		// ORIGINAL METHOD
		// if it was barely accepted (would have failed for 3/4 epsilon)
		// multiply tau by _p, such that 0 < p < _p < 1
		if (check_propensities((3.0/4.0) * epsilon)) {
			return (old_tau * 0.8);
		}
		// if tau was accepted for 3/4 epsilon
		// tau = old_tau ^ q (q = 0.5)
		else {
			return sqrt(old_tau);
		}

	} // end PostLeap
} // end compute tau 

// this method only classifies active rxns 
// assumes that the list active_rxns is up-to-date
vector<int> pla::reclassify_rxns(double tau) {
	// classify into ES, POISSON, LANGEVIN or DETERMINISTIC
	// or interchangeably VERY_SLOW, SLOW, MEDIUM or FAST
	double a_tau;
	vector<int> result;
	int i;
	for (list<int>::iterator it = active_rxns.begin(); it != active_rxns.end(); it++) {
		i = *it;
		a_tau = rxns[i].propensity * tau;
		//if (a_tau <= 1) {
		// setting a_tau limit to inf forces an exact stochastic simulation equivalent to SSA
		if (a_tau <= numeric_limits<double>::infinity()) {
			rxns[i].type = VERY_SLOW;
			result.push_back(i);
		}
		else if (a_tau > 1 && a_tau <= 100)
			rxns[i].type = SLOW;
		else if (sqrt(a_tau) >= 100)
			rxns[i].type = FAST;
		else if (a_tau > 100)
			rxns[i].type = MEDIUM;
	}
	return result;
} 

// type is rxns (0) or rules (1) 
// returns a vector of indices to nonzero rate ES rxns 
vector<int> pla::classify_rxns(double tau) {
	// classify into ES, POISSON, LANGEVIN or DETERMINISTIC
	// or interchangeably VERY_SLOW, SLOW, MEDIUM or FAST
	double a_tau;
	vector<int> result;
	num_zero_rxns = 0;
	for (int i = 0; i < rxns.size(); i++) {
		// catch inactive rxns
		// and push the rest into active_rxns vector
		if ( rxns[i].propensity == 0 ) {
			rxns[i].type = VERY_SLOW;
			num_zero_rxns++;
			active_rxns_map[i] = active_rxns.end();
			isActive.push_back(false);
			continue;
		}
		else {
			active_rxns.push_back(i);
			active_rxns_map[i] = --active_rxns.end();
			isActive.push_back(true);
		}
		a_tau = rxns[i].propensity * tau;

		//if (a_tau <= 1) {
		// setting a_tau limit to inf forces an exact stochastic simulation equivalent to SSA
		if (a_tau <= numeric_limits<double>::infinity()) {
			rxns[i].type = VERY_SLOW;
			//if ( rxns[i].propensity == 0 && rxns[i].tau == numeric_limits<double>::infinity() )
				//num_zero_rxns++;
			//else
			result.push_back(i);
		}
		else if (a_tau > 1 && a_tau <= 100)
			rxns[i].type = SLOW;
		else if (sqrt(a_tau) >= 100)
			rxns[i].type = FAST;
		else if (a_tau > 100)
			rxns[i].type = MEDIUM;
		}
/*
	else
		for (int i = 0; i < rules.size(); i++) {
			a_tau = rules[i].propensity * tau;
			if (a_tau <= 1)
				rules[i].type = VERY_SLOW;
			else if (a_tau > 1 && a_tau <= 100)
				rules[i].type = SLOW;
			else if (sqrt(a_tau) >= 100)
				rules[i].type = FAST;
			else if (a_tau > 100)
				rules[i].type = MEDIUM;
		}
*/
/*
	cout << "classes: ";
	for (int i = 0; i < rxns.size(); i++)
		cout << i+1 << "|" << rxns[i].type << " ";
	cout << endl;
*/
	return result;
}

// calculate derivative of propensity of rxn rxn_index with respect to concentration of species species_index 
// aproximated as the difference between a_j(x, x_i+1) and a_j(x)
double pla::deriv(int irxn, int i) {
	int k = rxns[irxn].r_stoich[i]; // number of times x_i appears as a reactant
	double ap, a; // propensities 'a prime' and 'a'
	a = rxns[irxn].propensity;
	double result = 0;
	x_offset[i+1]++;

	// if rxn's ratelaw depends on a function and x_i affects that function,
	// update the observables, functions and parameters appropriately
	// find the new propensity
	// then undo all update b/c they were hypothetical
	// update lists are indexed at base 1
	if (network.reactions->rxn[irxn]->rateLaw_type == FUNCTION) {
		vector<stack<int> > path = dependency_path(irxn, i);
		vector<int> functions_to_update;
		vector<int> observables_to_update;

		if (path.size() != 0) {
			for (int j = 0; j < path.size(); j++) {
				while (path[j].size() > 1) {
					path[j].pop();
					functions_to_update.push_back(path[j].top());
					path[j].pop();
				}
				if (path[j].size() == 1) {
					observables_to_update.push_back(path[j].top());
					path[j].pop();
				}
				else
					cout << "WARNING: PROBLEM WITH PATH RETURNED IN TAU LEAP DERIV" << endl;
			}

			remove_redundancies(observables_to_update);
			remove_redundancies(functions_to_update);
			vector<double> old_values_p;
			vector<double> old_values_o;
/*
			cout << "\nconcentrations: ";
			for (int q = 0; q < x.size(); q++)
				cout <<q+1 << "|" << network.species->elt[q]->val << " ";
			cout << "\nobservables:    ";
			for (int q = 0; q < network.n_groups; q++)
				cout << q+1 << "|" << network.spec_groups_vec[q]->total_val << " ";
			cout << "\nparameters:     ";
			for (int q = 0; q < network.rates->n_elt; q++)
				cout << q+1 << "|" << network.rates->elt[q]->val << " ";
			cout << "\nrates:          ";
			for (int q = 0; q < rxns.size(); q++)
				cout <<q+1 << "|" << rxns[q].propensity << " ";
			cout << endl;
*/
			// change species concentration
			network.species->elt[i]->val++;

			// update observables
			for (int q = 0; q < observables_to_update.size(); q++) {
				GROUP* obs = network.spec_groups_vec[ observables_to_update[q] - 1 ];
				old_values_o.push_back(obs->total_val);
				obs->total_val = 0;
				for (int j = 0; j < obs->n_elt; j++) {
					//cout << "updating group " << obs->name << " = " << obs->elt_factor[j] << " * "
						 //<< network.species->elt[ obs->elt_index[j] - 1 ]->val << endl;
					obs->total_val += obs->elt_factor[j] * network.species->elt[ obs->elt_index[j] - 1 ]->val;
				}
			}
      
			// update parameters
			for (int q = 0; q < functions_to_update.size(); q++) {
				int p = func_param_affect[ functions_to_update[q] - 1 ];
				old_values_p.push_back( network.rates->elt[p - 1]->val );
				network.rates->elt[p - 1]->val = network.functions[ functions_to_update[q] - 1 ].Eval();
			}
/*
			cout << "\nconcentrations: ";
			for (int q = 0; q < x.size(); q++)
				cout <<q+1 << "|" << network.species->elt[q]->val << " ";
			cout << "\nobservables:    ";
			for (int q = 0; q < network.n_groups; q++)
				cout << q+1 << "|" << network.spec_groups_vec[q]->total_val << " ";
			cout << "\nparameters:     ";
			for (int q = 0; q < network.rates->n_elt; q++)
				cout << q+1 << "|" << network.rates->elt[q]->val << " ";
			cout << "\nrates:          ";
			for (int q = 0; q < rxns.size(); q++)
				cout <<q+1 << "|" << rxns[q].propensity << " ";
			cout << endl;
*/
			// calculate new propensity
			ap = rxn_rate( network.reactions->rxn[irxn], x_offset, 1);
			//cout << "ap: " << ap << ", a: " << a << ", k: " << k << endl;

			//ap = network.rates->elt[ network.reactions->rxn[irxn]->rateLaw_indices[0] - 1 ]->val;

			// undo observable update
			for (int q = 0; q < old_values_o.size(); q++)
				network.spec_groups_vec[ observables_to_update[q] - 1 ]->total_val = old_values_o[q];

			// undo parameter updates
			for (int q = 0; q < old_values_p.size(); q++)
				network.rates->elt[ func_param_affect[ functions_to_update[q] - 1 ] - 1 ]->val = old_values_p[q];

			// undo species concentration change
			network.species->elt[i]->val--;
/*
			cout << "\nconcentrations: ";
			for (int q = 0; q < x.size(); q++)
				cout <<q+1 << "|" << network.species->elt[q]->val << " ";
			cout << "\nobservables:    ";
			for (int q = 0; q < network.n_groups; q++)
				cout << q+1 << "|" << network.spec_groups_vec[q]->total_val << " ";
			cout << "\nparameters:     ";
			for (int q = 0; q < network.rates->n_elt; q++)
				cout << q+1 << "|" << network.rates->elt[q]->val << " ";
			cout << "\nrates:          ";
			for (int q = 0; q < rxns.size(); q++)
				cout <<q+1 << "|" << rxns[q].propensity << " ";
			cout << endl;
*/
		}
		// rate law is a function, but reactant does not affect function
		else
			ap = rxn_rate(network.reactions->rxn[irxn], x_offset, 1);
	}
	// if rate law is not a function
	else
		ap = rxn_rate(network.reactions->rxn[irxn], x_offset, 1);

	x_offset[i+1]--;
	result = ap - a;
	return result;
} 

// update concentrations after reaction irxn fired n times 
//void pla::update_concentrations_TL(int irxn, int n, vector<int> &x_changed, int type) {
void pla::update_concentrations_TL(int irxn, int n, int type) {
	_out_ << "in update concentrations" << endl;

	int j;
	if (type == RXN_BASED) {
		for (int i = 0; i < network.reactions->rxn[irxn]->n_reactants; i++) {
			j = network.reactions->rxn[irxn]->r_index[i] - 1; // 0 based reactant index
			_out_ << "irxn " << irxn + 1 << " reactant " << j + 1 << endl;
			if (rxns[irxn].r_stoich[j] > 0 && !network.species->elt[j]->fixed) {
				//x_changed.push_back(j+1);
				x[j] -= n;
				x_offset[j+1] -= n;
			}
		}
		for (int i = 0; i < network.reactions->rxn[irxn]->n_products; i++) {
			j = network.reactions->rxn[irxn]->p_index[i] - 1; // 0 based product index
			_out_ << "irxn " << irxn + 1 << " product " << j + 1 << endl;
			if (rxns[irxn].p_stoich[j] > 0 && !network.species->elt[j]->fixed) {
				//x_changed.push_back(j+1);
				x[j] += n;
				x_offset[j+1] += n;
			}
		}
	}
/*
	else if (type == RULE_BASED) {
		for (int i = 0; i < rules[irxn].stoich.size(); i++) {
			if (abs(rules[irxn].stoich[i]) > 0) {
				x_faux[i] = x[i] + rules[irxn].stoich[i]*n;
				x_changed.push_back(i+1);
			}
		}
	}
*/
}

void pla::redo_update_concentrations(int irxn, int n) { 
	int j;
	for (int i = 0; i < network.reactions->rxn[irxn]->n_reactants; i++) {
		j = network.reactions->rxn[irxn]->r_index[i] - 1; // 0 based reactant index
		if (rxns[irxn].r_stoich[j] > 0) {
			x[j] += n;
			x_offset[j+1] += n;
		}
	}
	for (int i = 0; i < network.reactions->rxn[irxn]->n_products; i++) {
		j = network.reactions->rxn[irxn]->p_index[i] - 1; // 0 based product index
		if (rxns[irxn].p_stoich[j] > 0) {
			x[j] -= n;
			x_offset[j+1] -= n;
		}
	}
}

// RULE BASED VERSION 
// update propensities given that certain species have changed 
void pla::update_propensities_rules(vector<int> x_changed) {
	// find which observables have changed
	// and update observables (faux)
	vector<int> observables_to_update;
	for (int i = 0; i < x.size(); i++) {
		for (int j = 0; j < species_group_map[x_changed[i]].size(); j++)
			if (x_changed[i] != 0)
				observables_to_update.push_back( species_group_map[x_changed[i]][j] );
	}
	update_x_g(FAUX);
	remove_redundancies(observables_to_update);

	//cout << "updating observables: ";
	//for (int i = 0; i < observables_to_update.size(); i++)
		//cout << observables_to_update[i] << " "; cout << endl;

	cout << " ON YOUR MARKS " << endl;
	// update variable parameters (in increasing order)
	// get list of functions and parameters to update (functions may depend on parameters and vice versa)
	vector<int> functions_to_update;
	stack<int> param_stack;
	stack<int> func_stack;
	vector<int> params_to_update;
	// get list of functions from observables
	for (int i = 0; i < observables_to_update.size(); i++)
		for (int j = 0; j < observ_func_affect[ observables_to_update[i] - 1 ].size(); j++) {
			functions_to_update.push_back( observ_func_affect[ observables_to_update[i] - 1 ][j] );
			func_stack.push( observ_func_affect[ observables_to_update[i] - 1 ][j] );
		}

	// get list of functions from parameters
	while (!param_stack.empty() || !func_stack.empty()) {
		while (!func_stack.empty()) {
			param_stack.push( func_param_affect[ func_stack.top() - 1 ] );
			params_to_update.push_back( func_param_affect[ func_stack.top() - 1 ] );
			func_stack.pop();
		}
		while (!param_stack.empty()) {
			for (int i = 0; i < param_func_affect[ param_stack.top() - 1 ].size(); i++) {
				func_stack.push( param_func_affect[ param_stack.top() - 1 ][ i ] );
				functions_to_update.push_back( param_func_affect[ param_stack.top() - 1 ][ i ] );
			}
			param_stack.pop();
		}
	}

	cout << "GET SET " << endl;
	remove_redundancies(functions_to_update); // remove redundancies and order (ascending)
	remove_redundancies(params_to_update);

	// update variable parameters, evaluating function in ascending order
	for (int i = 0; i < functions_to_update.size(); i++)
		network.rates->elt[ func_param_affect[ functions_to_update[i] - 1 ] - 1 ]->val =
				network.functions[ functions_to_update[i] - 1 ].Eval();

	// determine which rules have changed because their base functional rate law has changed
	vector<int> rules_to_update;
	for (int i = 0; i < params_to_update.size(); i++)
		for (int j = 0; j < param_rule_affect[ params_to_update[i] - 1 ].size(); j++)
			rules_to_update.push_back( param_rule_affect[ params_to_update[i] - 1 ][ j ] - 1 );

	// determine which rules have changed because their reactant or product patterns have changed
	for (int i = 0; i <  observables_to_update.size(); i++)
		for (int j = 0; j < observ_rule_affect[ observables_to_update[i] - 1 ].size(); j++)
			rules_to_update.push_back(observ_rule_affect[ observables_to_update[i] - 1 ][j] - 1);
	remove_redundancies(rules_to_update);

	cout << "GO!" << endl;
	int jrxn;
	double anew;
	// update propensities
	// Loop over reactions in rxn update list updating both reaction and total rate
	for (int j = 0; j < rules_to_update.size(); j++) {
		jrxn= rules_to_update[j];
		if (rules[jrxn].rl_type == FUNCTION)
			rules[jrxn].base_a = network.rates->elt[ network.reactions->rxn[ jrxn ]->rateLaw_indices[0] - 1 ]->val *
				network.reactions->rxn[ jrxn ]->stat_factor;
		anew = rules[jrxn].base_a;
		if ( (rules[jrxn].rl_type == ELEMENTARY || rules[jrxn].rl_type == FUNCTION) && ( rules[jrxn].r_indices.size() == 2 ) &&
				(rules[jrxn].r_indices[0] == rules[jrxn].r_indices[1]) )
			anew *= x_g[ rules[jrxn].r_indices[0] - 1 ] * ( x_g[rules[jrxn].r_indices[0]-1] - 1.0 );
		else
			for (int i = 0; i < rules[jrxn].r_indices.size(); i++)
				anew *= x_g[rules[jrxn].r_indices[i]-1];

		delta_a[jrxn] = anew - pre_update_a[jrxn];
		rules[jrxn].propensity = anew;
	}
	return;
} 

// INPUT: list of updated observables OUTPUT: list of reactions to update 
// update variable parameters (in increasing order) 
// get list of functions and parameters to update (functions may depend on parameters and vice versa) 
vector<int> pla::calc_rxns_to_update (vector<int> observables_to_update) {

	for (int i = 0; i < network.functions.size(); i++)
		func_included[i] = false;

	vector<int> functions_to_update;
	stack<int> param_stack;
	stack<int> func_stack;
	vector<int> params_to_update;
	// get list of functions from observables
	for (int i = 0; i < observables_to_update.size(); i++)
		for (int j = 0; j < observ_func_affect[ observables_to_update[i] - 1 ].size(); j++) {
			if (!func_included[ observ_func_affect[ observables_to_update[i] - 1 ][j] - 1]) {
				functions_to_update.push_back( observ_func_affect[ observables_to_update[i] - 1 ][j] );
				func_included[ observ_func_affect[ observables_to_update[i] - 1 ][j] - 1] = true;
				func_stack.push( observ_func_affect[ observables_to_update[i] - 1 ][j] );
			}
		}

	// get list of functions from parameters
	while (!param_stack.empty() || !func_stack.empty()) {
		while (!func_stack.empty()) {
			param_stack.push( func_param_affect[ func_stack.top() - 1 ] );
			params_to_update.push_back( func_param_affect[ func_stack.top() - 1 ] );
			func_stack.pop();
		}
		while (!param_stack.empty()) {
			for (int i = 0; i < param_func_affect[ param_stack.top() - 1 ].size(); i++) {
				if (!func_included[param_func_affect[ param_stack.top() - 1 ][ i ] - 1]) {
					func_stack.push( param_func_affect[ param_stack.top() - 1 ][ i ] );
					func_included[param_func_affect[ param_stack.top() - 1 ][ i ] - 1] = true;
					functions_to_update.push_back( param_func_affect[ param_stack.top() - 1 ][ i ] );
				}
			}
			param_stack.pop();
		}
	}

	// sort variable parameters to update (order is important for update step)
	sort( functions_to_update.begin(), functions_to_update.end() );

	// update variable parameters, evaluating function in ascending order
	for (int i = 0; i < functions_to_update.size(); i++)
		network.rates->elt[ func_param_affect[ functions_to_update[i] - 1 ] - 1 ]->val = network.functions[ functions_to_update[i] - 1 ].Eval();

	// update reaction rates and total rate for rxns with functional rate laws
	vector<int> rxns_to_update;
	for (int i = 0; i < params_to_update.size(); i++)
		for (int j = 0; j < param_rxn_affect[ params_to_update[i] - 1 ].size(); j++)
			if (!rxn_included[param_rxn_affect[ params_to_update[i] - 1 ][ j ] - 1]) {
				rxns_to_update.push_back( param_rxn_affect[ params_to_update[i] - 1 ][ j ] - 1 );
				rxn_included[param_rxn_affect[ params_to_update[i] - 1 ][ j ] - 1] = true;
			}

	return rxns_to_update;
}

vector<int> test_vec; 
void pla::test() {

	cout << "n_rxn " << rxns.size() << endl;

	vector<pair<int, int> > rxns_fired;
	pair<int, int> temp;
	for (int i = 0; i < rxns.size(); i++) {
		temp.first = i;
		rxns_fired.push_back(temp);
		rxns[i].tau = rand() % 1000 + 5;
	}
 
	rxns[57].tau = 3;

	cout << "taus: ";
	for (int i = 0; i < rxns.size(); i++)
		cout << i+1 << "|" << rxns[i].tau << " ";
	cout << endl;
	//cout << "min tau index: " << find_min_tau(rxns_fired) << endl;
	exit(1);
/*
	vector<pair<int, int> > rxns_to_fire;

	pair<int, int> temp;
	cout << rxns.size() << endl;
	for (int i = 0; i < 2; i++) {
		temp.first = i;
		rxns_to_fire.push_back(temp);
	}
	cout << "rxns fired: " << endl;
	for (int i = 0; i < rxns_to_fire.size(); i++) {
		cout << rxns_to_fire[i].first << " ";
	}
	cout << endl;
	cout << "updating propensities: " << endl;
	for (int i = 0; i < rxns_to_fire.size(); i++)
		update_propensities( rxns_to_fire);
	cout << "functions updated: " << endl;

	remove_redundancies(test_vec);
	for (int i = 0; i < test_vec.size(); i++)
		cout << test_vec[i] << " ";
	cout << endl;
*/
} 

// update rxn indices are 0 based 
void pla::update_propensities(vector<pair<int, int> > rxns_fired) {
	for (int i = 0; i < network.n_groups; i++)
		observ_included[i] = false;
	for (int i = 0; i < rxns.size(); i++)
		rxn_included[i] = false;

	// update observables (in any order)
	// irxn is indexed in 0, groups are base 1
	// update observables
	int irxn;
	vector<int> observables_to_update;
	for (int j = 0; j < rxns_fired.size(); j++) {
		irxn = rxns_fired[j].first;
		for (int i = 0; i < rxn_observ_affect[irxn].size(); i++) {
			if ( !observ_included[rxn_observ_affect[irxn][i] - 1] ) {
				observables_to_update.push_back( rxn_observ_affect[irxn][i] );
				observ_included[rxn_observ_affect[irxn][i] - 1] = true;
			}
		}
	}
	for (int i = 0; i < observables_to_update.size(); i++) {
		//observ_included[observables_to_update[i] - 1] = false;
		network.spec_groups_vec[observables_to_update[i] - 1]->total_val = 0;
		for (int j = 0; j < network.spec_groups_vec[observables_to_update[i] - 1]->n_elt; j++)
			network.spec_groups_vec[observables_to_update[i] - 1]->total_val += network.spec_groups_vec[observables_to_update[i] - 1]->elt_factor[j] * x[network.spec_groups_vec[observables_to_update[i] - 1]->elt_index[j] - 1];
	}
  
	// figure out rxns to update based on groups (functional rate law value changed)
	vector<int> rxns_to_update = calc_rxns_to_update(observables_to_update);

	// figure out rxns to update based on rxns (reactant concentrations changed)
	for (int j = 0; j < rxns_fired.size(); j++) {
		irxn = rxns_fired[j].first;
		for (int i = 0; i < rxn_rxn_affect[irxn].size(); i++) {
			if (!rxn_included[rxn_rxn_affect[irxn][i]]) {
				rxns_to_update.push_back(rxn_rxn_affect[irxn][i]);
				rxn_included[rxn_rxn_affect[irxn][i]] = true;
			}
		}
	}
  
	//cout.setf(ios::fixed,ios::floatfield);
	//cout.precision(100);

	// update propensities
	int jrxn;
	double anew;
	// Loop over reactions in rxn update list updating both reaction and total rate
	for (int j = 0; j < rxns_to_update.size(); j++) {
		jrxn= rxns_to_update[j];
		rxn_included[ jrxn ] = false;
		anew= rxn_rate( network.reactions->rxn[jrxn], x_offset, 1);

		// inactive rxn becomes active
		// push it into list of active rxns and create map entry for it
		//if (*it == active_rxns.end() && anew > 0) {
		if (!isActive[jrxn] && anew > 0) {
			//cout << "activate rxn " << jrxn << " (base 0)" << endl;
			list<int>::iterator *it = &active_rxns_map[jrxn];
			active_rxns.push_back(jrxn);
			*it = --active_rxns.end();
			num_zero_rxns--;
			isActive[jrxn] = true;
		}
		// active rxns becomes inactive
		// remove it from list of active rxns and map it to active_rxns.end()
		//else if (*it != active_rxns.end() && ((rxns[jrxn].type == ES && rxns[jrxn].tau == numeric_limits<double>::infinity() &&
				//anew == 0 ) || (rxns[jrxn].type != ES && anew == 0))) {
		else if (isActive[jrxn] && ((rxns[jrxn].type == ES && rxns[jrxn].tau == numeric_limits<double>::infinity() && anew == 0 ) ||
				(rxns[jrxn].type != ES && anew == 0))) {

			//cout << "inactivate rxn " << jrxn << " (base 0)" << endl;
			list<int>::iterator *it = &active_rxns_map[jrxn];
			active_rxns.erase(*it);
			*it = active_rxns.end();
			num_zero_rxns++;
			isActive[jrxn] = false;
		}
		//cout << "active rxns (in prop) ";
		//for (list<int>::iterator it = active_rxns.begin(); it != active_rxns.end(); it++)
			//cout << *it << " ";
		//cout << endl;

		delta_a[jrxn] = anew - pre_update_a[jrxn];
		a_tot += anew - rxns[jrxn].propensity;
		//cout << "a_new " << anew << " " << " a_old " << rxns[jrxn].propensity << endl;
		rxns[jrxn].propensity = anew;
	}
	//cout << "end update propensities" << endl;
	_props_to_check = rxns_to_update;
	return;
}

// update rxn indices are 0 based 
void pla::update_propensities(int irxn) {
	//cout << "start update propensities" << endl;
	for (int i = 0; i < network.n_groups; i++)
		observ_included[i] = false;
	for (int i = 0; i < rxns.size(); i++)
		rxn_included[i] = false;

	// update observables (in any order)
	// irxn is indexed in 0, groups are base 1
	// update observables
	vector<int> observables_to_update;
	for (int i = 0; i < rxn_observ_affect[irxn].size(); i++) {
		if ( !observ_included[rxn_observ_affect[irxn][i] - 1] ) {
			observables_to_update.push_back( rxn_observ_affect[irxn][i] );
			observ_included[rxn_observ_affect[irxn][i] - 1] = true;
		}
	}

	for (int i = 0; i < observables_to_update.size(); i++) {
		//observ_included[observables_to_update[i] - 1] = false;
		network.spec_groups_vec[observables_to_update[i] - 1]->total_val = 0;
		for (int j = 0; j < network.spec_groups_vec[observables_to_update[i] - 1]->n_elt; j++)
			network.spec_groups_vec[observables_to_update[i] - 1]->total_val +=
					network.spec_groups_vec[observables_to_update[i] - 1]->elt_factor[j] *
					x[network.spec_groups_vec[observables_to_update[i] - 1]->elt_index[j] - 1];
	}
  
	// figure out rxns to update based on groups (functional rate law value changed)
	vector<int> rxns_to_update = calc_rxns_to_update(observables_to_update);

	// figure out rxns to update based on rxns (reactant concentrations changed)
	for (int i = 0; i < rxn_rxn_affect[irxn].size(); i++) {
		if (!rxn_included[rxn_rxn_affect[irxn][i]]) {
			rxns_to_update.push_back(rxn_rxn_affect[irxn][i]);
			rxn_included[rxn_rxn_affect[irxn][i]] = true;
		}
	}
  
	//cout.setf(ios::fixed,ios::floatfield);
	//cout.precision(100);

	// update propensities
	int jrxn;
	double anew;
	// Loop over reactions in rxn update list updating both reaction and total rate
	for (int j = 0; j < rxns_to_update.size(); j++) {
		jrxn= rxns_to_update[j];
		rxn_included[ jrxn ] = false;
		anew= rxn_rate( network.reactions->rxn[jrxn], x_offset, 1);

		// inactive rxn becomes active
		// push it into list of active rxns and create map entry for it
		//if (*it == active_rxns.end() && anew > 0) {
		if (!isActive[jrxn] && anew > 0) {
			//cout << "activate rxn " << jrxn << " (base 0)" << endl;
			list<int>::iterator *it = &active_rxns_map[jrxn];
			active_rxns.push_back(jrxn);
			*it = --active_rxns.end();
			num_zero_rxns--;
			isActive[jrxn] = true;

		}
		// active rxns becomes inactive
		// remove it from list of active rxns and map it to active_rxns.end()
		//else if (*it != active_rxns.end() && ((rxns[jrxn].type == ES && rxns[jrxn].tau == numeric_limits<double>::infinity() &&
				//anew == 0 ) || (rxns[jrxn].type != ES && anew == 0))) {
		else if (isActive[jrxn] && ((rxns[jrxn].type == ES && rxns[jrxn].tau == numeric_limits<double>::infinity() && anew == 0 ) ||
				(rxns[jrxn].type != ES && anew == 0))) {

			//cout << "inactivate rxn " << jrxn << " (base 0)" << endl;
			list<int>::iterator *it = &active_rxns_map[jrxn];
			active_rxns.erase(*it);
			*it = active_rxns.end();
			num_zero_rxns++;
			isActive[jrxn] = false;
		}
		//cout << "active rxns (in prop) ";
		//for (list<int>::iterator it = active_rxns.begin(); it != active_rxns.end(); it++)
			//cout << *it << " ";
		//cout << endl;

		//delta_a[jrxn] = anew - pre_update_a[jrxn];
		a_tot += anew - rxns[jrxn].propensity;
		//cout << "a_new " << anew << " " << " a_old " << rxns[jrxn].propensity << endl;
		rxns[jrxn].propensity = anew;
	}
	//cout << "end update propensities" << endl;
	//_props_to_check = rxns_to_update;
	return;
}

map<string, vector<int> > pla::get_rule_rxns_map() {
	map<string, vector<int> > result;
	string fileName = network.name;
	ifstream in(fileName.c_str());
	char line[1024];
	string temp;
	int index;
	while (in.getline(line, 1024) && strcmp(line, "begin reactions")) ;
	while (in.getline(line, 1024) && strcmp(line, "end reactions")) {
		istringstream in_str(line);
		in_str >> index;
		while ( in_str >> temp && temp[0] != '#');
		result[temp.substr(5, temp.size())].push_back(index);
	}
	return result;
} 

map<string, int> pla::get_param_map() {
	map<string, int> result;
	for (int i = 0; i < network.rates->n_elt; i++)
		result[network.rates->elt[i]->name] = network.rates->elt[i]->index;
	return result;
}

map<string, int> pla::get_observ_map() {
	map<string, int> result;
	for (int i = 0; i < network.spec_groups_vec.size(); i++)
		result[network.spec_groups_vec[i]->name] = network.spec_groups_vec[i]->index;
	return result;
}

void pla::read_rules() {
	cout << "STARTING TO READ RULES" << endl;
	string fileName = network.name;

	map<string, vector<int> > rule_rxns_map = get_rule_rxns_map();
	map<string, int> param_map = get_param_map();
	map<string, int> observ_map = get_observ_map();

	cout << "reading from " << fileName << endl;
	ifstream in(fileName.c_str());
	char line[1024];

	bool two_way;
	string name;
	string line_str;
	string temp;
	string rl1, rl2;
	while (in.getline(line, 1024) && strcmp(line, "begin reaction rules")) ;

	while (in.getline(line, 1024) && strcmp(line, "end reaction rules")) {
		vector<string> reactants, products;
		line_str = line;
		if (line[0] == '#') continue;
		if (!strcmp(line_str.substr(0,4).c_str(), "Rule")) {
			// name of rule(s)
			name = line_str[4];

			in.getline(line, 1024);
			line_str = line;
			vector<string> tokens;
			istringstream str_in(line_str);
			while (str_in >> temp) tokens.push_back(temp);
			bool read_r = false;
			for (int i = 0; i < tokens.size(); i++) {
				if (!read_r) {
					if (tokens[i][0] == '-') {
						two_way = false;
						read_r = true;
					}
					else if (tokens[i][0] == '<') {
						two_way = true;
						read_r = true;
					}
					else if (tokens[i] != "+")
						reactants.push_back(tokens[i]);
				}
				else {
					if (i < tokens.size() - (1 + two_way) )
						products.push_back(tokens[i]);
					else {
						if (two_way) {
							rl1 = tokens[i].substr(0,tokens[i].size()-1);
							rl2 = tokens[i+1];
							break;
						}
						else
							rl1 = tokens[i];
					}
				}
			}
			//cout << "about to push new rule " << endl;
			rule newRule(name, rules.size()+1, reactants, products, rl1, rule_rxns_map[name], &param_map, &observ_map, &x, x_offset);
			rules.push_back(newRule);
			if (two_way) {
				name += "r";
				rule newRule2(name, rules.size()+1, products, reactants, rl2, rule_rxns_map[name], &param_map, &observ_map, &x,
						x_offset);
				rules.push_back(newRule2);
			}
			//cout << "pushed new rule " << endl;
/*
			cout << "reactants: ";
			for (int i = 0; i < reactants.size(); i++)
				cout << reactants[i] << " "; cout << endl;

			cout << "products: ";
			for (int i = 0; i < products.size(); i++)
				cout << products[i] << " "; cout << endl;

			cout << "ratelaw(s): " << rl1 << " ";
			if (two_way) cout << rl2; cout << endl;
*/	
		}
	}

	cout << "rules: " << endl;
	for (int i = 0; i < rules.size(); i++) {
		cout << "rule " << i+1 << ", index: " << rules[i].index << ", name: " << rules[i].name << ", n_reactants: "
			 << rules[i].r_indices.size() << ", n_products: " << rules[i].p_indices.size() << ", first rxn: "
			 << rules[i].rxns_indices[0] << ", base rate: " << rules[i].base_a << ", total rate: " << rules[i].propensity
			 << ", rate law type: " << rules[i].rl_type << " ";
		//for (int j = 0; j < rules[i].stoich.size(); j++)
		//cout << j+1 << "|" << rules[i].stoich[j] << " ";
		cout << ", r indices: ";
		for (int j = 0; j < rules[i].r_indices.size(); j++)
			cout << rules[i].r_indices[j] << " ";
		cout << ", p indices: ";
		for (int j = 0; j < rules[i].p_indices.size(); j++)
			cout << rules[i].p_indices[j] << " ";

		cout << endl;
	}

	cout << "start init stoich" << endl;
	initialize_rule_stoich();
/*
	for (int i = 0; i < rules.size(); i++) {
		cout << i+1 << " stoich: ";
		for (int j = 0; j < rules[i].stoich.size(); j++)
			cout << j+1 << "|" << rules[i].stoich[j] << " ";cout << endl;
	}
	cout << endl;
*/
	cout << "DONE READING RULES " << endl;
/*
	cout << "network parameters: ";
	for (int i = 0; i < network.rates->n_elt; i++)
		cout << network.rates->elt[i]->name << "|" << network.rates->elt[i]->val << " "; cout << endl;
*/
	// initialize param_rule_affect and observ_rule_affect
	param_rule_affect = new vector<int>[network.rates->n_elt];
	for (int i = 0; i < rules.size(); i++)
		if (rules[i].rl_type == FUNCTION)
			param_rule_affect[ rules[i].rateLaw_indices[0] - 1 ].push_back( i + 1 );
  
	observ_rule_affect = new vector<int>[network.n_groups];
	for (int i = 0; i < rules.size(); i++) {
		for (int j = 0; j < rules[i].r_indices.size(); j++)
			observ_rule_affect[ rules[i].r_indices[ j ] - 1 ].push_back( i + 1 );
	}
}

rule::rule(string _name, int _index, vector<string> r_strings, vector<string> p_strings, string rl, vector<int> _rxns_indices,
		map<string, int> *param_map, map<string, int> *observ_map, vector<double> *x, double *x_offset ) {

	index = _index;
	rxns_indices = _rxns_indices;
	name = _name;
	// get observable indices from r and p strings
	// initialize r_indices and p_indices

	for (int i = 0; i < r_strings.size(); i++) {
		for (int j = 0; j < r_strings[i].size(); j++)
			if (r_strings[i][j] == '(' || r_strings[i][j] == ')')
				r_strings[i][j] = '_';
		//r_strings[i] = r_strings[i].substr(0,r_strings[i].size()-2);
		r_indices.push_back( (*observ_map)[ r_strings[i] ] );
	}
	for (int i = 0; i < p_strings.size(); i++) {
		//p_strings[i] = p_strings[i].substr(0,p_strings[i].size()-2);
		for (int j = 0; j < r_strings[i].size(); j++)
			if (r_strings[i][j] == '(' || r_strings[i][j] == ')')
				r_strings[i][j] = '_';
		p_indices.push_back( (*observ_map)[ p_strings[i] ] );
	}

	// parse rate law string to figure out rate law type, param indices and param values
	// determine the base propensity (total propensity / product of [reactant pattern]s)
	// this is constant for MM, Sat, Hill and Ele but changes for Fun

	cout << "rule # " << name << endl;
	cout << "network parameters: ";
	for (int i = 0; i < network.rates->n_elt; i++)
		cout << network.rates->elt[i]->name << "|" << network.rates->elt[i]->val << " "; cout << endl;
	cout << "species: ";
	for (int i = 0; i < network.species->n_elt; i++)
		cout << network.species->elt[i]->name << "|" << x_offset[i+1] << " "; cout << endl;
  
	rl_type = network.reactions->rxn[rxns_indices[0]-1]->rateLaw_type;

	int jrxn = rxns_indices[0];

	if (rl_type == MICHAELIS_MENTEN || rl_type == SATURATION || rl_type == HILL) {
		for (int i = 0; i < network.reactions->rxn[ jrxn - 1 ]->n_reactants; i++)
			x_offset[ network.reactions->rxn[jrxn-1]->r_index[i] ] = 1;
		base_a = rxn_rate( network.reactions->rxn[jrxn - 1], x_offset, 1);

		for (int i = 0; i < network.reactions->rxn[ jrxn - 1 ]->n_reactants; i++)
			x_offset[ network.reactions->rxn[jrxn-1]->r_index[i] ] = (*x)[ network.reactions->rxn[jrxn-1]->r_index[i] - 1 ];
	}
	// function or elementary
	else
		base_a = network.rates->elt[ network.reactions->rxn[ jrxn - 1 ]->rateLaw_indices[0] - 1 ]->val *
			network.reactions->rxn[ jrxn - 1 ]->stat_factor;
   
	// multiply base rate by reactant pattern concentrations to get full rate
	propensity = base_a;

	if ( (rl_type == ELEMENTARY || rl_type == FUNCTION) && ( r_indices.size() == 2 ) && (r_indices[0] == r_indices[1]) )
		propensity *= network.spec_groups_vec[ r_indices[0] - 1 ]->total_val *
			( network.spec_groups_vec[r_indices[0]-1]->total_val - 1.0 );
	else
		for (int i = 0; i < r_indices.size(); i++)
			propensity *= network.spec_groups_vec[r_indices[i]-1]->total_val;

	r_stoich.insert(r_stoich.begin(), x->size(), 0);
	p_stoich.insert(p_stoich.begin(), x->size(), 0);
  
	vector<string> eq_strings;
	eq_strings.insert(eq_strings.begin(), x->size(), "");
	// create equations strings
	// indicies in functions are base 1

	for (int i = 0; i < p_indices.size(); i++) {
		vector<string> numerators;
		numerators.insert( numerators.begin(), x->size(), "" );
		string sum = "";
		for (int j = 0; j < rxns_indices.size(); j++) {
			if (!sum.empty())
				sum += "+";
			else
				sum += "(";
			for (int k = 0; k < network.reactions->rxn[rxns_indices[j] - 1]->n_reactants; k++) {
				if (k != 0)
					sum += "*";
				ostringstream str_out(sum, ios_base::app);
				str_out << network.reactions->rxn[rxns_indices[j] - 1 ]->r_index[k];
				sum = str_out.str();
			}
			for (int k = 0; k < network.reactions->rxn[rxns_indices[j] - 1]->n_products; k++) {
				if (!numerators[ network.reactions->rxn[rxns_indices[j] - 1 ]->p_index[k] -1 ].empty() )
					numerators[ network.reactions->rxn[rxns_indices[j] - 1 ]->p_index[k] -1 ] += "+";
				else
					numerators[ network.reactions->rxn[rxns_indices[j] - 1 ]->p_index[k] -1 ] += "(";
				for (int q = 0; q < network.reactions->rxn[rxns_indices[j] - 1]->n_reactants; q++) {
					if (q != 0)
						numerators[ network.reactions->rxn[rxns_indices[j] - 1 ]->p_index[k] -1 ] += "*";
					ostringstream str_out(numerators[ network.reactions->rxn[rxns_indices[j] - 1 ]->p_index[k] -1 ], ios_base::app);
					str_out << network.reactions->rxn[rxns_indices[j] - 1 ]->r_index[q];
					numerators[ network.reactions->rxn[rxns_indices[j] - 1 ]->p_index[k] -1 ] = str_out.str();
				}
			}
		}

		for (int j = 0; j < x->size(); j++)
			if (!numerators[j].empty())
				numerators[j] += ")";

		sum += ")";
		for (int j = 0; j < x->size(); j++) {
			if (!numerators[j].empty()) {
				numerators[j] += "/";
				numerators[j] += sum;
				if (!eq_strings[j].empty())
					eq_strings[j] += "+";
				eq_strings[j] += numerators[j];
			}
		}
	}

	cout << "equation strings: ";
	for (int i = 0; i < eq_strings.size(); i++)
		cout << i+1 << "| " << eq_strings[i] << " "; cout << endl;

	// make parsers
	// link variables

	//p_stoich_eqs
/*
	vector<int> reacts;
	for (int i = 0; i < r_indices.size(); i++) {
		for (int j = 0; j < network.spec_groups_vec[ r_indices[i] - 1 ]->n_elt; j++) {
			reacts.push_back( network.spec_groups_vec[ r_indices[i] - 1 ]->elt_index[j] );
		}
	}
	remove_redundancies(reacts);
	cout << "reactant species: ";
	for (int i = 0; i < reacts.size(); i++)
		cout << reacts[i] << " "; cout << endl;
	vector<double> temp_vec;
*/
	//temp_vec.insert()

	// for each reaction in the rule, check out its reactants
	// for each reactant,

	//r_stoich
}
