package view

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.chaquo.python.Python
import com.example.inprogress.R
import kotlinx.android.synthetic.main.fragment_valuations.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ValuationsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ValuationsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_valuations, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lateinit var option: Spinner
        //lateinit var result: TextView

        option = view!!.findViewById(R.id.optionType)
        //result = view!!.findViewById(R.id.mptRes)

        val options = getResources().getStringArray(R.array.Valuation)

        option.adapter = ArrayAdapter<String>(activity!!.applicationContext, android.R.layout.simple_list_item_1, options)


        option.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onNothingSelected(parent: AdapterView<*>?) {
                return null!!
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {}
        }

            fun initOPV(): String{

                var callorput = option.getSelectedItem().toString()
                val python = Python.getInstance()
                val pythonFile = python.getModule("OptionValuation_py")
                println(optionType)

                return pythonFile.callAttr("OPV",  initialPrice.text.toString(),
                    strikePrice.text.toString(),
                    riskFreeRate.text.toString(),
                    timeHorizon.text.toString(),
                    callorput).toString()
            }

            valuationBtn.setOnClickListener {
                if (initialPrice.text.isNotEmpty() && strikePrice.text.isNotEmpty() &&
                    riskFreeRate.text.isNotEmpty() && timeHorizon.text.isNotEmpty()) {
                        val intent = Intent(view.context, ValuationsRes::class.java)
                        var valuation = initOPV()
                        if (!valuation.contains("Error")) {
                            println(intent)
                            intent.putExtra("res", valuation)
                            startActivity(intent)
                        } else {
                            Toast.makeText(activity, valuation, Toast.LENGTH_LONG).show()
                        }

                    } else {
                        Toast.makeText(activity, "Error: Missing at least 1 parameter!", Toast.LENGTH_LONG).show()
                }
            }

        }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ValuationsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ValuationsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}