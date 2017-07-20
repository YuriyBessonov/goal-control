package app.warinator.goalcontrol.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import app.warinator.goalcontrol.database.DAO.TrackUnitDAO;
import app.warinator.goalcontrol.model.TrackUnit;

/**
 * Адаптер автодополнения единиц учета прогресса
 */
public class UnitsAutocompleteAdapter extends BaseAdapter implements Filterable {

    private Context mContext;
    private List<TrackUnit> mUnits;

    public UnitsAutocompleteAdapter(Context context) {
        mContext = context;
        mUnits = new ArrayList<>();
    }

    @Nullable
    @Override
    public TrackUnit getItem(int position) {
        return mUnits.get(position);
    }

    @Override
    public int getCount() {
        return mUnits.size();
    }

    @Override
    public long getItemId(int position) {
        return mUnits.get(position).getId();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(android.R.layout.simple_dropdown_item_1line, parent,
                    false);
        }
        TrackUnit unit = getItem(position);
        if (unit != null) {
            ((TextView) convertView.findViewById(android.R.id.text1)).setText(unit.getName());
        }
        return convertView;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public Filter getFilter() {
        final Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults res = new FilterResults();
                if (constraint != null) {
                    List<TrackUnit> units = findUnits(constraint.toString());
                    res.values = units;
                    res.count = units.size();
                }
                return res;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    mUnits = (List<TrackUnit>) results.values;
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
        return filter;
    }

    private List<TrackUnit> findUnits(String str) {
        return TrackUnitDAO.getDAO().getAllStartingWith(str, false).toBlocking().first();
    }
}

