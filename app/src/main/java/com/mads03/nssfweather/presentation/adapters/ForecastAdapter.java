package com.mads03.nssfweather.presentation.adapters;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.mads03.nssfweather.R;
import com.mads03.nssfweather.models.Weather;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;


public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.MyViewHolder> {

  private List<Weather> weatherList;
  private LayoutInflater inflater;
  Context context;
  private ForecastAdapterListener listener;
  private SparseBooleanArray selectedItems;

  // array used to perform multiple animation at once
  private SparseBooleanArray animationItemsIndex;
  private boolean reverseAllAnimations = false;

  // index is used to animate only the selected row
  // dirty fix, find a better solution
  private static int currentSelectedIndex = -1;

  public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
    public TextView forecastDate, forecastTemp;
    public ImageView forecastIcon;
    private CardView listItemCardContainer;

    public MyViewHolder(View view) {
      super(view);
      forecastDate = view.findViewById(R.id.forecast_date_tv);
      forecastTemp = view.findViewById(R.id.forecast_temp_tv);
      forecastIcon = view.findViewById(R.id.forecast_temp_icon);
      listItemCardContainer = view.findViewById(R.id.forecast_list_item_container);
      view.setOnLongClickListener(this);
    }

    @Override
    public boolean onLongClick(View view) {
      view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
      return true;
    }
  }

  public ForecastAdapter(Context context, List<Weather> weather, ForecastAdapterListener listener) {
    inflater = LayoutInflater.from(context);
    this.context = context;
    this.weatherList = weather;
    this.listener = listener;
    selectedItems = new SparseBooleanArray();
    animationItemsIndex = new SparseBooleanArray();
  }

  @Override
  public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View itemView = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.forecast_list_item, parent, false);

    return new MyViewHolder(itemView);
  }

  @Override
  public void onBindViewHolder(final MyViewHolder holder, int position) {
    Weather weather = weatherList.get(position);
    //if (!weather.getIsCurrent().equals("1")){
      // displaying text view data

      holder.forecastTemp.setText(weather.getMax());

      SimpleDateFormat savedFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

      SimpleDateFormat readableFormat = new SimpleDateFormat("EEEE", Locale.getDefault());

      try{
        Date d = savedFormat.parse(weather.getDate());
        d.setTime(d.getTime());
        //myFormat.setTimeZone(timeZone);
        holder.forecastDate.setText(readableFormat.format(d));
      }catch (Exception e){
        e.printStackTrace();
      }

      switch (weather.getShortDesc()){
        case "Rain":
          holder.forecastIcon.setBackgroundResource(R.drawable.rain);
          holder.listItemCardContainer.setCardBackgroundColor(context.getResources().getColor(R.color.rainy));
          break;
        case "Clouds":
          holder.forecastIcon.setBackgroundResource(R.drawable.partlysunny);
          holder.listItemCardContainer.setCardBackgroundColor(context.getResources().getColor(R.color.cloudy));
          break;
        case "Clear":
          holder.forecastIcon.setBackgroundResource(R.drawable.clear);
          holder.listItemCardContainer.setCardBackgroundColor(context.getResources().getColor(R.color.sunny));
          break;
        default:
          holder.forecastIcon.setBackgroundResource(R.drawable.clear);
          holder.listItemCardContainer.setCardBackgroundColor(context.getResources().getColor(R.color.sunny));

      }
    //}

  }

  public void setList(List<Weather> weather) {
    this.weatherList = weather;
    notifyDataSetChanged();
  }

  public void swapWeatherForecast(final List<Weather> newWeatherList) {
    // If there was no forecast data, then recreate all of the list
    if (weatherList == null) {
      weatherList = newWeatherList;
      notifyDataSetChanged();
    }
            /*
        } else {
            /*
             * Otherwise we use DiffUtil to calculate the changes and update accordingly. This
             * shows the four methods you need to override to return a DiffUtil callback. The
             * old list is the current list stored in mForecast, while the new list is the new
             * values passed in from observing the database.
             */

    DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
      @Override
      public int getOldListSize() {
        return weatherList.size();
      }

      @Override
      public int getNewListSize() {
        return newWeatherList.size();
      }

      @Override
      public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return weatherList.get(oldItemPosition).getWeatherId() ==
                newWeatherList.get(newItemPosition).getWeatherId();
      }

      @Override
      public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        Weather newWeather = newWeatherList.get(newItemPosition);
        Weather oldWeather = weatherList.get(oldItemPosition);
        return newWeather.getWeatherId() == oldWeather.getWeatherId();
        // && newCategory.getCategoryName().equals(oldCategory.getCategoryName());
      }
    });
    weatherList = newWeatherList;
    result.dispatchUpdatesTo(this);
  }

  @Override
  public long getItemId(int position) {
    return weatherList.get(position).getWeatherId();
  }

  @Override
  public int getItemCount() {
    if(weatherList == null) return 0;

    return weatherList.size();
  }


  public void removeData(int position) {
    weatherList.remove(position);
    resetCurrentIndex();
  }

  private void resetCurrentIndex() {
    currentSelectedIndex = -1;
  }

  public interface ForecastAdapterListener {

    //void onJobRowClicked(int position);

  }

}
