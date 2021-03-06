import numpy as np
import pandas as pd
import scipy.stats as scs
import matplotlib.pyplot as plt
from PIL import Image
import requests
from bs4 import BeautifulSoup
import json
from Static import SP500
import os


def VAR(select):

    ticker = SP500.get(select)

    try:
        url_prefix = 'https://sandbox.iexapis.com/stable/stock/'
        url_suffix = '/chart/max?token=Tsk_d536dffef19e4ae4941ea4ac530d6133'
        full_url = '{}{}{}'.format(url_prefix, ticker.lower(), url_suffix)
        source = requests.get(full_url, timeout=20)
        soup = BeautifulSoup(source.text, 'html.parser')
        data = json.loads(str(soup))

    except (requests.exceptions.Timeout, requests.exceptions.ConnectionError):
        return ['error', 'Error: Internet connection failure.']
    except (ValueError, requests.exceptions.HTTPError):
        return ['error', 'Error: {}{}'.format(full_url, '\ndoes not exist.')]
    except:
        return ['error', 'Error parsing IEX API. Please contact the creator for its resolution.']

    if not data:
        error_msg = 'Error: IEX API cannot load price data for {}.'.format(select)
        return ['error', error_msg]


    data = pd.DataFrame(data, columns=['date', 'close'])
    data.rename(columns={'date': 'Date', 'close': select}, inplace=True)


    plt.figure(figsize=(10, 10))
    data = pd.DataFrame(data)

    latest_date = data.values[-1][0]
    latest_price = data.values[-1][1]
    simulation = 10000

    percs = np.linspace(0, 100, num=simulation)

    percs_display =     {0.01: int((0.01/100)*len(percs)), 0.1: int((0.1/100)*len(percs)),
                         1: int((1/100)*len(percs)), 2.5: int((2.5/100)*len(percs)),
                         5: int((5/100)*len(percs)), 10: int((10/100)*len(percs)),
                         20: int((20/100)*len(percs)), 30: int((30/100)*len(percs)),
                         40: int((40/100)*len(percs)), 50: int((50/100)*len(percs))}


    risk = np.log(data[select] / data[select].shift(1))

    VaR = scs.scoreatpercentile(latest_price * risk, percs)

    _, _, bars = plt.hist(VaR, bins=500)
    for bar in bars:
        if bar.get_x() > 0:
            bar.set_facecolor("cornflowerblue")
        else:
            bar.set_facecolor("tomato")


    plt.title('Value at Risk Profile: {}'.format(select))
    plt.xlabel('Return')
    plt.ylabel('Frequency')


    dir = '{}{}'.format(os.environ["HOME"], '/vargraph.png')
    plt.savefig(dir)


    image = Image.open(dir)

    width, height = image.size
    left = width / 18
    top = height / 18
    right = width
    bottom = height
    image = image.crop((left, top, right, bottom))

    image.rotate(270).save(dir)

    result = str(format(simulation, ',')) + ' simulations generated.\n\n'
    description = 'Value at Risk profile for {} that is trading at {} on {}.\n\n'\
        .format(select, format(round(latest_price, 2)), str(latest_date))

    title = '%16s %16s' % ('Confidence Level', 'Loss Amount')
    line = 'ー' * 16

    res = []
    for k, v in percs_display.items():
        res.append('{}{}{}|{}{}'.format(' ' * 6, format(100 - k, '.4f'),
                                        ' ' * 13, ' ' * 7, format(-VaR[v], ',.2f')))

    return [dir, '{}{} {}\n{}\n{}'.format(result, description, title, line, '\n'.join(res))]
