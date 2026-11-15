package com.treinocerto.app.ui.common

import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.treinocerto.app.R
import com.treinocerto.app.data.local.PreferenciasTema
import com.treinocerto.app.databinding.LayoutTelaBaseBinding
import com.treinocerto.app.util.visivel

/**
 * Activity base de todas as telas do app. Ela:
 * - coloca o conteúdo da tela abaixo de uma barra superior (Toolbar) com título e seta de voltar;
 * - reserva o espaço da barra de status, da barra de navegação e do teclado, para que nada
 *   fique escondido atrás delas (o Android 15+ desenha o app de ponta a ponta);
 * - mostra o botão de tema claro/escuro no canto superior direito.
 */
abstract class TelaBase : AppCompatActivity() {

    /** Telas com cabeçalho próprio (Login e Home) escondem a barra superior. */
    protected open val mostrarBarraSuperior = true

    private val preferenciasTema by lazy { PreferenciasTema(this) }

    override fun setContentView(view: View) {
        val base = LayoutTelaBaseBinding.inflate(layoutInflater)
        base.conteudoTela.addView(
            view,
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT),
        )
        super.setContentView(base.root)

        setSupportActionBar(base.barraSuperior)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        base.barraSuperior.visivel(mostrarBarraSuperior)
        aplicarEspacosDoSistema(base)
    }

    /** Afasta o conteúdo das barras do sistema e do teclado (WindowInsets). */
    private fun aplicarEspacosDoSistema(base: LayoutTelaBaseBinding) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false // ícones claros sobre a faixa verde
            isAppearanceLightNavigationBars = !preferenciasTema.estaEscuro()
        }
        ViewCompat.setOnApplyWindowInsetsListener(base.raizTela) { _, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            val teclado = insets.getInsets(WindowInsetsCompat.Type.ime())
            base.areaSuperior.updatePadding(top = barras.top)
            base.raizTela.updatePadding(
                left = barras.left,
                right = barras.right,
                bottom = maxOf(barras.bottom, teclado.bottom),
            )
            WindowInsetsCompat.CONSUMED
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (!mostrarBarraSuperior) return false
        menuInflater.inflate(R.menu.menu_tela, menu)
        menu.findItem(R.id.acaoTema)?.setIcon(iconeDoTema())
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.acaoTema) {
            preferenciasTema.alternar() // a tela é recriada com o novo tema
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /** Seta de voltar da barra superior: fecha a tela atual. */
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    /** Ícone que indica o tema para o qual o botão vai trocar. */
    protected fun iconeDoTema() =
        if (preferenciasTema.estaEscuro()) R.drawable.ic_tema_claro else R.drawable.ic_tema_escuro

    /** Alterna o tema (usado pelos botões dos cabeçalhos da Home e do Login). */
    protected fun alternarTema() = preferenciasTema.alternar()
}
