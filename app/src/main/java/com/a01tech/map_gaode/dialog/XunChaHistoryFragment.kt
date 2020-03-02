package com.a01tech.map_gaode.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.a01tech.map_gaode.MapApp
import com.a01tech.map_gaode.R
import com.a01tech.map_gaode.fragments.XunChaBean
import com.a01tech.map_gaode.retrofit.RetrofitUtil
import com.a01tech.map_gaode.utils.DialogUtil
import com.a01tech.map_gaode.utils.JsonData
import com.a01tech.map_gaode.utils.ToastUtil
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.google.gson.JsonObject
import com.qmuiteam.qmui.util.QMUIDisplayHelper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_popup_xuncha_history.*
import java.text.SimpleDateFormat

private const val ARG_PARAM1 = "param1"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [BlankFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [BlankFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class XunChaHistoryFragment : DialogFragment() {

    private var param1: String? = null

    lateinit var adapter: NormalHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.layout_popup_xuncha_history, container, false)
    }

    override fun onStart() {
        super.onStart()
        getDialog().getWindow().setLayout(QMUIDisplayHelper.dpToPx(400), ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        adapter = NormalHistoryAdapter()
        rcv.layoutManager = LinearLayoutManager(context)
        rcv.adapter = adapter
        adapter.setOnItemChildClickListener { _, view, position ->
            val dialog = DialogUtil.genLoadingDialog(context!!)
            dialog.show()
            RetrofitUtil.getService()
                .xunchaDetail(
                    MapApp.user.token,
                    adapter.data[position].id
                ).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .map { JsonData(it) }
                .subscribe({
                    dialog.dismiss()
                    if (it.code == 1) {
                        val d = it.getData("map", XunChaBean::class.java)
                        d?.id = adapter.data[position].id

                        XunChaDetailFragment.newInstance(MapApp.gson.toJson(d)).show(fragmentManager, "")
                    } else {
                        ToastUtil.showToast(context, it.msg)
                    }
                }, {
                    it.printStackTrace()
                    ToastUtil.showToast(context, "ERROR")
                    dialog.dismiss()
                })
        }


        val data = JsonData(MapApp.gson.fromJson(param1, JsonObject::class.java))
        val list = data.getDataList("list", XunChaHistoryBean::class.java)
        adapter.replaceData(list)

        btnBack.setOnClickListener {
            dismiss()
        }

    }


    companion object {
        @JvmStatic
        fun newInstance(s: String) =
            XunChaHistoryFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, s)
                }
            }
    }


}


class NormalHistoryAdapter() : BaseQuickAdapter<XunChaHistoryBean, BaseViewHolder>(R.layout.item_xunchajilu) {
    var format = SimpleDateFormat("yy-MM-dd HH:mm:ss")
    override fun convert(helper: BaseViewHolder, item: XunChaHistoryBean) {
        try {
            if (item.time.contains("-"))
                helper.setText(R.id.text1, item.time)
            else helper.setText(R.id.text1, format.format(item.time.toLong()))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        helper.setText(R.id.text4, item.content)
        helper.addOnClickListener(R.id.btn1)
    }
}

