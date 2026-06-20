const currencyFormatter = new Intl.NumberFormat("es-CL", {
  style: "currency",
  currency: "CLP",
  minimumFractionDigits: 2,
  maximumFractionDigits: 2
});

const dateFormatter = new Intl.DateTimeFormat("es-CL", {
  day: "2-digit",
  month: "long",
  year: "numeric"
});

export function formatCurrency(value) {
  return currencyFormatter.format(Number(value ?? 0));
}

export function formatLongDate(value) {
  if (!value) {
    return "--";
  }

  return dateFormatter.format(new Date(`${value}T00:00:00`));
}
