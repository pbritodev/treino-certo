package com.treinocerto.app.ui.peso

import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import com.treinocerto.app.R
import com.treinocerto.app.util.Formatos

/**
 * View personalizada que desenha um gráfico de linha da evolução do peso.
 * Usa apenas Canvas (sem bibliotecas externas). A linha tracejada laranja é a meta.
 */
class GraficoPesoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    data class Ponto(val rotulo: String, val valor: Double)

    private var pontos: List<Ponto> = emptyList()
    private var meta: Double? = null

    private val densidade = resources.displayMetrics.density
    private val corLinha = ContextCompat.getColor(context, R.color.verde_primario)
    private val corMeta = ContextCompat.getColor(context, R.color.laranja)
    private val corTexto = ContextCompat.getColor(context, R.color.texto_secundario)

    private val pincelLinha = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = corLinha
        strokeWidth = 3 * densidade
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }
    private val pincelArea = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val pincelPonto = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = corLinha }
    private val pincelMeta = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = corMeta
        strokeWidth = 2 * densidade
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(8 * densidade, 6 * densidade), 0f)
    }
    // Objetos reutilizados a cada desenho (evita alocar memória dentro do onDraw)
    private val linha = Path()
    private val area = Path()

    private val pincelTexto = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = corTexto
        textSize = 11 * densidade
    }

    /** O degradê depende da altura da View, então é recriado só quando o tamanho muda. */
    override fun onSizeChanged(largura: Int, altura: Int, larguraAntiga: Int, alturaAntiga: Int) {
        super.onSizeChanged(largura, altura, larguraAntiga, alturaAntiga)
        pincelArea.shader = LinearGradient(
            0f, 0f, 0f, altura.toFloat(),
            ColorUtils.setAlphaComponent(corLinha, 90), ColorUtils.setAlphaComponent(corLinha, 0),
            Shader.TileMode.CLAMP,
        )
    }

    fun definirDados(novosPontos: List<Ponto>, pesoMeta: Double?) {
        pontos = novosPontos
        meta = pesoMeta
        invalidate() // pede para o Android redesenhar a View
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (pontos.size < 2) return

        val margemEsq = 36 * densidade
        val margemDir = 12 * densidade
        val margemTopo = 16 * densidade
        val margemBase = 24 * densidade
        val largura = width - margemEsq - margemDir
        val altura = height - margemTopo - margemBase

        val valores = pontos.map { it.valor } + listOfNotNull(meta)
        val minimo = valores.min() - 1
        val maximo = valores.max() + 1
        fun x(i: Int) = margemEsq + largura * i / (pontos.size - 1)
        fun y(valor: Double) = (margemTopo + altura * (1 - (valor - minimo) / (maximo - minimo))).toFloat()

        // Rótulos do eixo Y (mínimo e máximo)
        canvas.drawText(Formatos.decimal(maximo), 0f, margemTopo + pincelTexto.textSize / 2, pincelTexto)
        canvas.drawText(Formatos.decimal(minimo), 0f, margemTopo + altura, pincelTexto)

        meta?.let {
            canvas.drawLine(margemEsq, y(it), margemEsq + largura, y(it), pincelMeta)
        }

        linha.reset()
        pontos.forEachIndexed { i, ponto ->
            if (i == 0) linha.moveTo(x(i), y(ponto.valor)) else linha.lineTo(x(i), y(ponto.valor))
        }

        // Área preenchida com degradê abaixo da linha
        area.set(linha)
        area.lineTo(x(pontos.lastIndex), margemTopo + altura)
        area.lineTo(x(0), margemTopo + altura)
        area.close()
        canvas.drawPath(area, pincelArea)
        canvas.drawPath(linha, pincelLinha)

        // Pontos e rótulos do eixo X (no máximo ~6 datas para não sobrepor)
        val passo = maxOf(1, pontos.size / 6)
        pontos.forEachIndexed { i, ponto ->
            canvas.drawCircle(x(i), y(ponto.valor), 4 * densidade, pincelPonto)
            if (i % passo == 0 || i == pontos.lastIndex) {
                val largTexto = pincelTexto.measureText(ponto.rotulo)
                val posX = (x(i) - largTexto / 2).coerceIn(margemEsq - largTexto / 2, width - largTexto)
                canvas.drawText(ponto.rotulo, posX, height - 6 * densidade, pincelTexto)
            }
        }
    }
}
