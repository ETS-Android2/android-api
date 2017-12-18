package com.wrld.widgets.searchbox;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ListView;
import android.widget.TextView;

import com.wrld.widgets.R;
import com.wrld.widgets.ui.UiScreenController;

import java.util.ArrayList;

class SearchResultScreenController implements UiScreenController {

    private LayoutInflater m_inflater;
    private ViewGroup m_rootContainer;

    private ViewGroup m_searchResultContainer;
    private ViewGroup m_autoCompleteResultContainer;

    private ArrayList<PaginatedSearchResultsController> m_searchResultControllers;
    private ArrayList<SuggestionSearchResultController> m_suggestionControllers;

    private Animation m_showAnim;
    private Animation m_hideAnim;

    private ScreenState m_screenState;

    public ScreenState getScreenState() { return m_screenState; }

    private SearchModuleController m_searchModuleMediator;

    SearchResultScreenController(ViewGroup resultSetsContainer, SearchModuleController searchModuleMediator){

        m_rootContainer = resultSetsContainer;
        m_inflater = LayoutInflater.from(m_rootContainer.getContext());
        m_searchResultContainer = (ViewGroup)resultSetsContainer.findViewById(R.id.searchbox_search_results_container);
        m_autoCompleteResultContainer = (ViewGroup)resultSetsContainer.findViewById(R.id.searchbox_autocomplete_container);

        m_searchModuleMediator = searchModuleMediator;

        m_showAnim = new Animation(){
            @Override
            public void start() {
                super.start();
                m_rootContainer.setVisibility(View.VISIBLE);
                m_screenState = ScreenState.VISIBLE;
            }
        };
        m_hideAnim = new Animation(){
            @Override
            public void start() {
                super.start();
                hideSuggestionSets();
                m_rootContainer.setVisibility(View.GONE);
                m_screenState = ScreenState.GONE;
            }
        };

        m_searchResultControllers = new ArrayList<PaginatedSearchResultsController>();
        m_suggestionControllers = new ArrayList<SuggestionSearchResultController>();
        m_screenState = ScreenState.GONE;
    }

    public void removeAllSearchProviderViews(){
        m_searchResultContainer.removeAllViews();
        m_searchResultControllers.clear();
    }

    public void removeAllAutocompleteProviderViews(){
        m_autoCompleteResultContainer.removeAllViews();
        m_suggestionControllers.clear();
    }

    public SearchResultsController inflateViewForSearchProvider(
            SearchResultSet resultSet,
            SearchResultViewFactory viewFactory){
        // Cannot add view here with flag as we need to specify the index for layout to work
        View setView = m_inflater.inflate(R.layout.search_result_set, m_searchResultContainer, false);
        m_searchResultContainer.addView(setView, m_searchResultControllers.size() );
        View setContent = setView.findViewById(R.id.searchbox_set_content);
        ListView listView = (ListView) setContent.findViewById(R.id.searchbox_set_result_list);

        final PaginatedSearchResultsController resultsController = new PaginatedSearchResultsController(
                setView, resultSet, viewFactory);

        m_searchResultControllers.add(resultsController);
        listView.setAdapter(resultsController);
        return resultsController;
    }

    public SearchResultsController inflateViewForAutoCompleteProvider(
            String title,
            SearchResultSet resultSet,
            SearchResultViewFactory viewFactory){
        // Cannot add view here with flag as we need to specify the index for layout to work
        View setView = m_inflater.inflate(R.layout.search_suggestion_set, m_autoCompleteResultContainer, false);
        m_autoCompleteResultContainer.addView(setView, m_suggestionControllers.size());
        ListView listView = (ListView) setView.findViewById(R.id.searchbox_set_result_list);

        final SuggestionSearchResultController resultsController = new SuggestionSearchResultController(
                setView, resultSet, viewFactory);

        TextView titleView = (TextView)setView.findViewById(R.id.search_set_title);
        titleView.setText(title);

        m_suggestionControllers.add(resultsController);
        listView.setAdapter(resultsController);
        return resultsController;
    }

    public void showResults(){
        hideSuggestionSets();
        m_searchResultContainer.setVisibility(View.VISIBLE);

        for(PaginatedSearchResultsController searchResultController : m_searchResultControllers){
            searchResultController.searchStarted();
        }
    }

    public void showAutoComplete(){
        m_autoCompleteResultContainer.setVisibility(View.VISIBLE);
        m_searchResultContainer.setVisibility(View.GONE);
    }

    @Override
    public Animation transitionToVisible() {
        m_showAnim.reset();
        return m_showAnim;
    }

    @Override
    public Animation transitionToGone() {
        m_hideAnim.reset();
        return m_hideAnim;
    }

    private void hideSuggestionSets(){
        m_autoCompleteResultContainer.setVisibility(View.GONE);
        for(SuggestionSearchResultController suggestionController : m_suggestionControllers){
            suggestionController.hide();
        }
    }
}
