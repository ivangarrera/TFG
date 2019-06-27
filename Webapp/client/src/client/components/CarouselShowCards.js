import React from 'react'
import MyCard from './GeneralInfoExpeditionCard'

class CarouselShowCards extends React.Component {
    render() {
        return (
            <div className="row">
                <div className='col-md-1' />
                <div className='col-md-10'>
                    <div id="my-carousel" className="carousel slide" data-ride="carousel" style={{backgroundColor: '#143449', height: '200px'}}>
                        <ol className="carousel-indicators">
                            <li data-target="#my-carousel" data-slide-to="0" className="active"></li>
                            <li data-target="#my-carousel" data-slide-to="1"></li>
                            <li data-target="#my-carousel" data-slide-to="2"></li>
                            <li data-target="#my-carousel" data-slide-to="3"></li>
                            <li data-target="#my-carousel" data-slide-to="4"></li>
                        </ol>
                        <div className="carousel-inner">
                            <div className="carousel-item active">
                                <div className="row">
                                    <div className="col-md-2 col-2" />
                                    <div className="col-md-8 col-8">
                                        <MyCard card_identifier='1' />
                                    </div>
                                </div>
                                <div className="col-md-2" />
                            </div>

                            <div className="carousel-item">
                                <div className="row">
                                    <div className="col-md-2 col-2" />
                                    <div className="col-md-8 col-8">
                                        <MyCard card_identifier='2' />
                                    </div>
                                </div>
                                <div className="col-md-2" />
                            </div>

                            <div className="carousel-item">
                                <div className="row">
                                    <div className="col-md-2 col-2" />
                                    <div className="col-md-8 col-8">
                                        <MyCard card_identifier='3' />
                                    </div>
                                </div>
                                <div className="col-md-2 col-2" />
                            </div>

                            <div className="carousel-item">
                                <div className="row">
                                    <div className="col-md-2 col-2" />
                                    <div className="col-md-8 col-8">
                                        <MyCard card_identifier='4' />
                                    </div>
                                </div>
                                <div className="col-md-2 col-2" />
                            </div>

                            <div className="carousel-item">
                                <div className="row">
                                    <div className="col-md-2 col-2" />
                                    <div className="col-md-8 col-8">
                                        <MyCard card_identifier='5' />
                                    </div>
                                </div>
                                <div className="col-md-2 col-2" />
                            </div>
                            
                    </div>
                    <a className="carousel-control-prev" href="#my-carousel" role="button" data-slide="prev">
                        <span className="carousel-control-prev-icon" aria-hidden="true"></span>
                        <span className="sr-only">Previous</span>
                    </a>
                    <a className="carousel-control-next" href="#my-carousel" role="button" data-slide="next">
                        <span className="carousel-control-next-icon" aria-hidden="true"></span>
                        <span className="sr-only">Next</span>
                    </a>
                </div>
            </div>
            <div className='col-md-1' />
            </div >
        );
    }
}

export default CarouselShowCards;