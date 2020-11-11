package app.rootstock.ui.main

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import app.rootstock.R
import app.rootstock.data.network.ReLogInObservable
import app.rootstock.data.network.ReLogInObserver
import app.rootstock.ui.settings.SettingsActivity
import app.rootstock.ui.signup.RegisterActivity
import app.rootstock.utils.convertDpToPx
import com.google.android.material.bottomnavigation.BottomNavigationMenu
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class WorkspaceActivity : AppCompatActivity(), ReLogInObserver {

    @ExperimentalCoroutinesApi
    private val viewModel: WorkspaceViewModel by viewModels()

    @Inject
    lateinit var reLogInObservable: ReLogInObservable


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_workspace)

        findViewById<FloatingActionButton>(R.id.fab)?.apply {
            shapeAppearanceModel =
                shapeAppearanceModel.withCornerSize { 30f }
        }

        findViewById<BottomNavigationView>(R.id.bottom_app_bar)?.apply {
            setOnNavigationItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_home -> viewModel.navigateToRoot()
                    R.id.menu_settings -> navigateToSettings()
                }
                true
            }
        }
        setObservers()

    }

    private fun navigateToSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun setObservers() {
        viewModel.pagerPosition.observe(this) {
            if (it == null) return@observe
            animateFab(it)
        }
    }

    private fun animateFab(position: Int) {
        if (position > 1) return
        val toCircle = position == 1
        // 10f - round dps for square button
        // 30f - for circle button
        val startEnd = if (toCircle) 10f to 30f else 30f to 10f
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        lifecycleScope.launch {
            ObjectAnimator.ofFloat(fab, "interpolation", startEnd.first, startEnd.second).apply {
                duration = ANIMATION_DURATION_FAB
                addUpdateListener { animator ->
                    fab.apply {
                        shapeAppearanceModel =
                            shapeAppearanceModel.withCornerSize { (convertDpToPx(animator.animatedValue as Float)) }
                    }
                }
            }.start()
        }
    }

    override fun onStart() {
        super.onStart()
        reLogInObservable.addObserver(this)
    }

    override fun onStop() {
        super.onStop()
        reLogInObservable.removeObserver(this)
    }

    override fun submit() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
        finishAfterTransition()
    }

    companion object {
        // 200 ms
        const val ANIMATION_DURATION_FAB = 200L
    }

}
