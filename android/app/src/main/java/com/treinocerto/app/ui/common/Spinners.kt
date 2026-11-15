package com.treinocerto.app.ui.common

import android.widget.ArrayAdapter
import android.widget.Spinner
import com.treinocerto.app.data.model.OpcaoApi

/** Preenche um Spinner com os rótulos de um enum e permite ler o item escolhido. */
fun <T : OpcaoApi> Spinner.configurarOpcoes(opcoes: List<T>, selecionado: T? = null) {
    val adaptador = ArrayAdapter(context, android.R.layout.simple_spinner_item, opcoes.map { it.rotulo })
    adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    adapter = adaptador
    tag = opcoes
    selecionado?.let { setSelection(opcoes.indexOf(it)) }
}

@Suppress("UNCHECKED_CAST")
fun <T : OpcaoApi> Spinner.opcaoSelecionada(): T = (tag as List<T>)[selectedItemPosition]
