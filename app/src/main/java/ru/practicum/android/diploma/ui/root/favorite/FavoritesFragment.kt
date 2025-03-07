package ru.practicum.android.diploma.ui.root.favorite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.data.db.converter.FavoriteVacancyConverter
import ru.practicum.android.diploma.databinding.FragmentFavoritesBinding
import ru.practicum.android.diploma.domain.models.Vacancy
import ru.practicum.android.diploma.domain.models.VacancyDetail
import ru.practicum.android.diploma.presentation.favorite.FavoriteScreenState
import ru.practicum.android.diploma.presentation.favorite.FavoriteViewModel
import ru.practicum.android.diploma.ui.root.details.DetailsFragment.Companion.VACANCY_ID
import ru.practicum.android.diploma.ui.root.search.VacancyAdapter

class FavoritesFragment : Fragment() {
    private val viewModel: FavoriteViewModel by viewModel()
    private val binding by lazy { FragmentFavoritesBinding.inflate(layoutInflater) }
    private val adapter by lazy { VacancyAdapter(mutableListOf()) { onFavoriteVacancyClick(it) } }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getFavoriteVacancies()
        viewModel.favoriteScreenState.observe(viewLifecycleOwner) { state ->
            when (state) {
                FavoriteScreenState.EmptyList -> showEmpty()
                FavoriteScreenState.Error -> showError()
                is FavoriteScreenState.FavoriteVacancies -> showFavorite(state.favoriteVacancies)
            }
        }
        binding.favoriteRecyclerView.adapter = adapter
    }

    private fun onFavoriteVacancyClick(it: Vacancy) {
        findNavController().navigate(
            R.id.action_favoritesFragment_to_detailsFragment,
            bundleOf(VACANCY_ID to it.id)
        )
    }

    private fun showFavorite(favoriteVacancies: List<VacancyDetail>) {
        with(binding) {
            favoritePlaceholder.visibility = View.GONE
            favoriteError.visibility = View.GONE
            favoriteRecyclerView.visibility = View.VISIBLE
        }
        adapter.updateData(favoriteVacancies.map { FavoriteVacancyConverter.vacancyDetailToVacancy(it) })
    }

    private fun showError() {
        with(binding) {
            favoritePlaceholder.setImageResource(R.drawable.placeholder_no_vacancy)
            favoriteError.setText(R.string.favorire_error)
            favoriteRecyclerView.visibility = View.GONE
            favoritePlaceholder.visibility = View.VISIBLE
            favoriteError.visibility = View.VISIBLE
        }
    }

    private fun showEmpty() {
        with(binding) {
            favoritePlaceholder.setImageResource(R.drawable.placeholder_empty_list)
            favoriteError.setText(R.string.favorite_empty)
            favoriteRecyclerView.visibility = View.GONE
            favoritePlaceholder.visibility = View.VISIBLE
            favoriteError.visibility = View.VISIBLE
        }
    }

}
