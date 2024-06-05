package com.braze.advancedsamples.inapp.modal

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.*
import com.braze.Braze
import com.braze.advancedsamples.R
import com.braze.ui.inappmessage.views.InAppMessageModalView

class TeamPickerView(private val ctx: Context, attrs:AttributeSet): InAppMessageModalView(ctx, attrs), View.OnClickListener {

    lateinit var cancelBtn: Button
    lateinit var selectBtn: Button
    lateinit var spinner:Spinner

    override fun onFinishInflate() {
        super.onFinishInflate()
        spinner = findViewById(R.id.team_spinner)
        cancelBtn = findViewById(R.id.dismiss_button)
        cancelBtn.text = "Dismiss"
        selectBtn = findViewById(R.id.select_button)
        selectBtn.text = "Select"
        selectBtn.setOnClickListener(this)
    }

    fun setTeams(teams:List<String>){
        spinner.adapter = ArrayAdapter(ctx, android.R.layout.simple_expandable_list_item_1, teams)
    }

    fun setTitle(title:String){
        val titleView = findViewById<TextView>(R.id.team_picker_title)
        titleView.text = title
    }

    override fun onClick(v: View?) {
        val selectedTeam = spinner.selectedItem as String;
        Braze.getInstance(ctx).currentUser?.setCustomUserAttribute("FavoriteTeam", selectedTeam)
        messageClickableView?.performClick()
    }





}