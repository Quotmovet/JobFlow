package ru.practicum.android.diploma.ui.root.search

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentSearchBinding
import ru.practicum.android.diploma.domain.models.Vacancy
import ru.practicum.android.diploma.presentation.search.SearchEventState
import ru.practicum.android.diploma.presentation.search.SearchScreenState
import ru.practicum.android.diploma.presentation.search.SearchViewModel
import ru.practicum.android.diploma.ui.root.details.DetailsFragment.Companion.VACANCY_ID
import java.text.DecimalFormat

class SearchFragment : Fragment() {
    private val viewModel: SearchViewModel by viewModel()
    private val binding by lazy { FragmentSearchBinding.inflate(layoutInflater) }
    private val adapter by lazy { VacancyAdapter(mutableListOf()) { selectVacancy(it) } }

    private fun selectVacancy(vacancy: Vacancy) {
        findNavController().navigate(
            R.id.action_mainFragment_to_detailsFragment,
            bundleOf(VACANCY_ID to vacancy.id)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.searchEditText.requestFocus()

        viewModel.searchScreenState.observe(viewLifecycleOwner) { state ->
            when (state) {
                SearchScreenState.LoadingFirstPage -> showLoading()
                SearchScreenState.NoInternetFirstPage -> showNoInternet()
                SearchScreenState.NothingFound -> showNothingFound()
                SearchScreenState.ErrorFirstPage -> showError()
                is SearchScreenState.Results -> showResults(state.resultsList, state.totalCount)
                SearchScreenState.LoadingNextPage -> showLoadingNextPage()
            }
        }

        viewModel.event.observe(viewLifecycleOwner){
            when(it){
                SearchEventState.EndOfListReached -> showProblemNextPage(END_OF_LIST)
                SearchEventState.ErrorNextPage -> showProblemNextPage(ERROR)
                SearchEventState.NoInternetNextPage -> showProblemNextPage(NO_INTERNET)
            }
        }

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrBlank()) {
                    binding.searchMagnifier.setImageResource(R.drawable.ic_close)
                    viewModel.searchDebounce(s.toString())
                } else {
                    binding.searchMagnifier.setImageResource(R.drawable.ic_search)
                }
            }

            override fun afterTextChanged(s: Editable?) = Unit
        }

        with(binding) {
            searchRecyclerView.adapter = adapter
            searchEditText.addTextChangedListener(textWatcher)
            searchMagnifier.setOnClickListener {
                searchEditText.setText(EMPTY_TEXT)
                setKeyboardVisibility(searchEditText, false)
            }

            searchRecyclerView.addOnScrollListener(object :
                androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager =
                        recyclerView.layoutManager as? androidx.recyclerview.widget.LinearLayoutManager ?: return

                    val totalItemCount = layoutManager.itemCount
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                    if (lastVisibleItemPosition == totalItemCount - 1 && dy > 0) {
                        onEndOfListReached()
                    }
                }
            })

            binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    viewModel.searchDebounce(searchEditText.text.toString())
                    setKeyboardVisibility(searchEditText, false)
                    true
                } else {
                    false
                }
            }
        }
    }

    private fun onEndOfListReached() {
        viewModel.getNextPage()
    }

    private fun showLoadingNextPage() {
        with(binding) {
            searchProgressBarBottom.visibility = View.VISIBLE
        }
    }

    private fun showProblemNextPage(type: String) {
        with(binding) {
            searchProgressBarBottom.visibility = View.GONE
            val toastMessage = when (type) {
                NO_INTERNET -> getString(R.string.check_internet)
                END_OF_LIST -> getString(R.string.end_of_list)
                else -> getString(R.string.error_occupied)
            }
            showToast(toastMessage)
        }
    }

    private fun showError() {
        with(binding) {
            searchImgPlaceholder.setImageResource(R.drawable.placeholder_error)
            searchError.setText(R.string.error)
            searchProgressBar.visibility = View.GONE
            searchRecyclerView.visibility = View.GONE
            searchVacancyCount.visibility = View.GONE
            searchProgressBarBottom.visibility = View.GONE
            searchImgPlaceholder.visibility = View.VISIBLE
            searchError.visibility = View.VISIBLE
        }
    }

    private fun showResults(vacancies: List<Vacancy>, totalCount: Int) {
        with(binding) {
            searchImgPlaceholder.visibility = View.GONE
            searchError.visibility = View.GONE
            searchProgressBar.visibility = View.GONE
            searchProgressBarBottom.visibility = View.GONE
            val formattedCount = DecimalFormat("#,###").format(totalCount)
            searchVacancyCount.text = "${
                resources.getQuantityString(
                    R.plurals.count_postfix_found,
                    totalCount,
                    totalCount
                )
            } " + "$formattedCount ${
                resources.getQuantityString(
                    R.plurals.count_postfix_vacancy, totalCount, totalCount
                )
            }"
            searchVacancyCount.visibility = View.VISIBLE
            searchRecyclerView.visibility = View.VISIBLE
        }
        adapter.updateData(vacancies)
    }

    private fun showNoInternet() {
        with(binding) {
            searchImgPlaceholder.setImageResource(R.drawable.placeholder_no_internet)
            searchError.setText(R.string.no_internet)
            searchProgressBar.visibility = View.GONE
            searchRecyclerView.visibility = View.GONE
            searchVacancyCount.visibility = View.GONE
            searchProgressBarBottom.visibility = View.GONE
            searchImgPlaceholder.visibility = View.VISIBLE
            searchError.visibility = View.VISIBLE
        }
    }

    private fun showNothingFound() {
        with(binding) {
            searchImgPlaceholder.setImageResource(R.drawable.placeholder_no_vacancy)
            searchError.setText(R.string.nothing_found)
            searchVacancyCount.setText(R.string.no_vacancies)
            searchProgressBar.visibility = View.GONE
            searchRecyclerView.visibility = View.GONE
            searchProgressBarBottom.visibility = View.GONE
            searchImgPlaceholder.visibility = View.VISIBLE
            searchError.visibility = View.VISIBLE
            searchVacancyCount.visibility = View.VISIBLE
        }
    }

    private fun showLoading() {
        with(binding) {
            searchRecyclerView.visibility = View.GONE
            searchImgPlaceholder.visibility = View.GONE
            searchError.visibility = View.GONE
            searchVacancyCount.visibility = View.GONE
            searchProgressBarBottom.visibility = View.GONE
            searchProgressBar.visibility = View.VISIBLE
            setKeyboardVisibility(searchEditText, false)
        }
    }

    private fun setKeyboardVisibility(view: View, isVisible: Boolean) {
        val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if (isVisible) {
            inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        } else {
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun showToast(message: String) {
        val snackBar = Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT)
        snackBar.setTextColor(requireContext().getColor(R.color.black))
        val backgroundSnackbar = snackBar.view.apply { setBackgroundResource(R.drawable.background_snack_bar)}
        val textSnackbar: TextView = backgroundSnackbar.findViewById(com.google.android.material.R.id.snackbar_text)
        textSnackbar.textAlignment = View.TEXT_ALIGNMENT_CENTER
        snackBar.show()
    }

    override fun onResume() {
        super.onResume()
        with(binding) {
            searchEditText.requestFocus()
            setKeyboardVisibility(searchEditText, true)
        }

    }

    companion object {
        const val EMPTY_TEXT = ""
        const val NO_INTERNET = "NO_INTERNET"
        const val ERROR = "ERROR"
        const val END_OF_LIST = "END_OF_LIST"
    }
}
